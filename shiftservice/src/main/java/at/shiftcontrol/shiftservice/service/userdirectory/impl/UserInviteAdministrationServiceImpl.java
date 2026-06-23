package at.shiftcontrol.shiftservice.service.userdirectory.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import at.shiftcontrol.lib.common.UniqueCodeGenerator;
import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.entity.UserInviteShiftPlanAccess;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteCreateDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteSearchDto;
import at.shiftcontrol.shiftservice.mapper.PaginationMapper;
import at.shiftcontrol.shiftservice.mapper.UserInviteMapper;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.role.RoleRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteRepository;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteAdministrationService;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteClaimService;

@Service
@RequiredArgsConstructor
public class UserInviteAdministrationServiceImpl implements UserInviteAdministrationService {
    private static final String INVITE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 10;
    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 10;

    private final UserInviteRepository userInviteRepository;
    private final ShiftPlanRepository shiftPlanRepository;
    private final RoleRepository roleRepository;
    private final UniqueCodeGenerator uniqueCodeGenerator;
    private final UserInviteClaimService userInviteClaimService;

    @Override
    @AdminOnly
    @Transactional(readOnly = true)
    public @NonNull PaginationDto<UserInviteDto> getAllInvites(int page, int size, @NonNull UserInviteSearchDto searchDto) {
        var inviteIdPage = userInviteRepository.searchInviteIds(
            searchDto.getStatus(),
            toSearchPattern(searchDto.getName()),
            PageRequest.of(page, size)
        );
        if (inviteIdPage.isEmpty()) {
            return PaginationMapper.toPaginationDto(size, page, inviteIdPage.getTotalElements(), List.of());
        }

        List<UserInviteDto> paginatedItems = getOrderedInvitesByIds(inviteIdPage.getContent()).stream()
            .map(UserInviteMapper::toDto)
            .toList();
        return PaginationMapper.toPaginationDto(size, page, inviteIdPage.getTotalElements(), paginatedItems);
    }

    @Override
    @AdminOnly
    @Transactional
    public @NonNull UserInviteDto createInvite(@NonNull UserInviteCreateDto createDto) {
        Instant now = Instant.now();
        if (createDto.getExpiresAt() != null && createDto.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("expiresAt must be in the future");
        }

        Set<Long> roleIds = toLongIds(createDto.getRoleIds());
        Collection<Role> pendingRoles = roleIds.isEmpty() ? List.of() : roleRepository.findAllById(roleIds);
        if (pendingRoles.size() != roleIds.size()) {
            throw new BadRequestException("One or more roleIds are invalid");
        }

        var invite = UserInvite.builder()
            .code(generateInviteCode())
            .email(createDto.getEmail().trim())
            .preferredUsername(trimToNull(createDto.getPreferredUsername()))
            .firstName(trimToNull(createDto.getFirstName()))
            .lastName(trimToNull(createDto.getLastName()))
            .displayName(trimToNull(createDto.getDisplayName()))
            .status(UserInviteStatus.PENDING)
            .createdAt(now)
            .expiresAt(createDto.getExpiresAt())
            .pendingRoles(pendingRoles)
            .build();

        Set<Long> accessPlanIds = createDto.getShiftPlanAccesses().stream()
            .map(access -> ConvertUtil.idToLong(access.getShiftPlanId()))
            .collect(Collectors.toSet());
        var shiftPlansById = shiftPlanRepository.findAllById(accessPlanIds).stream()
            .collect(Collectors.toMap(at.shiftcontrol.lib.entity.ShiftPlan::getId, java.util.function.Function.identity()));
        if (shiftPlansById.size() != accessPlanIds.size()) {
            throw new BadRequestException("One or more shiftPlanIds are invalid");
        }

        for (var access : createDto.getShiftPlanAccesses()) {
            long shiftPlanId = ConvertUtil.idToLong(access.getShiftPlanId());
            invite.addPendingShiftPlanAccess(UserInviteShiftPlanAccess.builder()
                .shiftPlan(shiftPlansById.get(shiftPlanId))
                .accessType(access.getAccessType())
                .build());
        }

        validateRoleCoverage(invite);

        try {
            invite = userInviteRepository.save(invite);
        } catch (DataIntegrityViolationException e) {
            invite.setCode(generateInviteCode());
            invite = userInviteRepository.save(invite);
        }
        userInviteClaimService.invalidatePendingInviteEmailCache(invite.getEmail());

        return UserInviteMapper.toDto(invite);
    }

    @Override
    @AdminOnly
    @Transactional
    public void revokeInvite(long inviteId) {
        UserInvite invite = userInviteRepository.findById(inviteId)
            .orElseThrow(() -> new NotFoundException("User invite not found."));
        if (invite.getStatus() == UserInviteStatus.CLAIMED) {
            throw new BadRequestException("Claimed invites cannot be revoked.");
        }
        if (invite.getStatus() == UserInviteStatus.REVOKED) {
            return;
        }

        invite.setStatus(UserInviteStatus.REVOKED);
        invite.setRevokedAt(Instant.now());
        userInviteRepository.save(invite);
        userInviteClaimService.invalidatePendingInviteEmailCache(invite.getEmail());
    }

    private void validateRoleCoverage(UserInvite invite) {
        if (invite.getPendingRoles() == null || invite.getPendingRoles().isEmpty()) {
            return;
        }

        Set<Long> accessibleShiftPlanIds = invite.getPendingShiftPlanAccesses().stream()
            .filter(access -> access.getAccessType() != at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType.LOCKED
                || invite.getPendingShiftPlanAccesses().stream()
                .anyMatch(other -> other.getShiftPlan().getId() == access.getShiftPlan().getId()
                    && other.getAccessType() != at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType.LOCKED))
            .map(access -> access.getShiftPlan().getId())
            .collect(Collectors.toSet());

        List<String> invalidRoleNames = invite.getPendingRoles().stream()
            .filter(role -> !accessibleShiftPlanIds.contains(role.getShiftPlan().getId()))
            .map(Role::getName)
            .toList();

        if (!invalidRoleNames.isEmpty()) {
            throw new BadRequestException("Invite is missing volunteer or planner access for roles: " + String.join(", ", invalidRoleNames));
        }
    }

    private Set<Long> toLongIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return ids.stream().map(ConvertUtil::idToLong).collect(Collectors.toSet());
    }

    private String generateInviteCode() {
        return uniqueCodeGenerator.generateUnique(
            INVITE_CODE_ALPHABET,
            INVITE_CODE_LENGTH,
            MAX_INVITE_CODE_GENERATION_ATTEMPTS,
            userInviteRepository::existsByCode
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<UserInvite> getOrderedInvitesByIds(Collection<Long> inviteIds) {
        Map<Long, UserInvite> invitesById = userInviteRepository.findAllDetailedByIdIn(inviteIds).stream()
            .peek(this::initializeInviteAssociations)
            .collect(Collectors.toMap(UserInvite::getId, java.util.function.Function.identity()));
        return inviteIds.stream()
            .map(invitesById::get)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    private void initializeInviteAssociations(UserInvite invite) {
        invite.getPendingRoles().size();
        invite.getPendingShiftPlanAccesses().forEach(access -> access.getShiftPlan().getName());
        if (invite.getClaimedUserAccount() != null && invite.getClaimedUserAccount().getExternalIdentities() != null) {
            invite.getClaimedUserAccount().getExternalIdentities().size();
        }
    }

    private String toSearchPattern(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return "%" + name.trim().toLowerCase() + "%";
    }
}
