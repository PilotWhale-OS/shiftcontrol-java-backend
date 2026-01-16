package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import at.shiftcontrol.lib.common.UniqueCodeGenerator;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.ShiftPlanInvite;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.ShiftPlanEvent;
import at.shiftcontrol.lib.event.events.ShiftPlanInviteEvent;
import at.shiftcontrol.lib.event.events.ShiftPlanVolunteerEvent;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.lib.type.ShiftPlanInviteType;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanInviteDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDetailsDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanCreateDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanModificationDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.InviteMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final AssignmentService assignmentService;

    private final EventDao eventDao;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftPlanInviteDao shiftPlanInviteDao;
    private final RoleDao roleDao;
    private final VolunteerDao volunteerDao;

    private final SecurityHelper securityHelper;
    private final ApplicationUserProvider userProvider;
    private final ApplicationEventPublisher publisher;
    private final UserAttributeProvider userAttributeProvider;

    private final UniqueCodeGenerator uniqueCodeGenerator;

    // URL-safe, human-friendly alphabet (no 0/O, 1/I to reduce confusion)
    private static final String INVITE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 10;

    @Override
    public Collection<ShiftPlanDto> getAll(long eventId) {
        eventDao.getById(eventId);
        return ShiftPlanMapper.toShiftPlanDto(shiftPlanDao.findByEventId(eventId));
    }

    @Override
    public ShiftPlanDto get(long shiftPlanId) {
        return ShiftPlanMapper.toShiftPlanDto(shiftPlanDao.getById(shiftPlanId));
    }

    @Override
    @AdminOnly
    public ShiftPlanCreateDto createShiftPlan(long eventId, ShiftPlanModificationDto modificationDto) {
        var event = eventDao.getById(eventId);
        var plan = ShiftPlanMapper.toShiftPlan(modificationDto);
        plan.setEvent(event);
        plan.setLockStatus(LockStatus.SELF_SIGNUP);
        plan = shiftPlanDao.save(plan);

        // create unspecific invites for volunteer and planner by default
        var volunteerInvite = ShiftPlanInvite.builder()
            .code(callGenerateUniqueCode())
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .shiftPlan(plan)
            .active(true)
            .uses(0)
            .createdAt(Instant.now())
            .build();
        volunteerInvite = shiftPlanInviteDao.save(volunteerInvite);

        var plannerInvite = ShiftPlanInvite.builder()
            .code(callGenerateUniqueCode())
            .type(ShiftPlanInviteType.PLANNER_JOIN)
            .shiftPlan(plan)
            .active(true)
            .uses(0)
            .createdAt(Instant.now())
            .build();
        plannerInvite = shiftPlanInviteDao.save(plannerInvite);

        publisher.publishEvent(ShiftPlanEvent.of(RoutingKeys.SHIFTPLAN_CREATED, plan));
        var shiftPlanDto = ShiftPlanMapper.toShiftPlanDto(plan);

        return ShiftPlanCreateDto.builder()
            .shiftPlan(shiftPlanDto)
            .volunteerInvite(InviteMapper.toInviteDto(volunteerInvite, plan))
            .plannerInvite(InviteMapper.toInviteDto(plannerInvite, plan))
            .build();
    }

    @Override
    @AdminOnly
    public ShiftPlanDto update(long shiftPlanId, ShiftPlanModificationDto modificationDto) {
        var plan = shiftPlanDao.getById(shiftPlanId);
        ShiftPlanMapper.updateShiftPlan(modificationDto, plan);
        shiftPlanDao.save(plan);
        publisher.publishEvent(ShiftPlanEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_UPDATED,
            Map.of("shiftPlanId", String.valueOf(shiftPlanId))), plan));
        return ShiftPlanMapper.toShiftPlanDto(plan);
    }

    @Override
    @AdminOnly
    public void delete(long shiftPlanId) {
        var shiftPlan = shiftPlanDao.getById(shiftPlanId);

        var invites = shiftPlanInviteDao.findAllByShiftPlanId(shiftPlanId);
        for (var invite : invites) {
            var inviteEvent = ShiftPlanInviteEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_DELETED,
                Map.of("shiftPlanId", String.valueOf(shiftPlanId),
                    "inviteId", String.valueOf(invite.getId()))), invite);
            shiftPlanInviteDao.delete(invite);
            publisher.publishEvent(inviteEvent);
        }

        var shiftPlanEvent = ShiftPlanEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_DELETED,
            Map.of("shiftPlanId", String.valueOf(shiftPlanId))), shiftPlan);
        shiftPlanDao.delete(shiftPlan);
        publisher.publishEvent(shiftPlanEvent);
    }

    @Override
    public ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto) {
        var currentUser = userProvider.getCurrentUser();
        validatePermission(shiftPlanId, requestDto.getType(), currentUser);
        final var shiftPlan = shiftPlanDao.getById(shiftPlanId);
        if (requestDto.getExpiresAt() != null && requestDto.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("expiresAt must be in the future");
        }
        if (requestDto.getMaxUses() != null && requestDto.getMaxUses() <= 0) {
            throw new BadRequestException("maxUses must be positive");
        }
        // Generate a unique code (retry a few times)
        String code = callGenerateUniqueCode();
        Collection<Role> rolesToAssign = List.of();
        if (requestDto.getAutoAssignRoleIds() != null && !requestDto.getAutoAssignRoleIds().isEmpty()) {
            rolesToAssign = roleDao.findAllById(requestDto.getAutoAssignRoleIds().stream().map(ConvertUtil::idToLong).toList());
            if (rolesToAssign.size() != requestDto.getAutoAssignRoleIds().size()) {
                throw new BadRequestException("One or more roleIds are invalid");
            }
            if (!rolesToAssign.stream().allMatch(role -> role.getShiftPlan().getId() == shiftPlanId)) {
                throw new BadRequestException("One or more roles do not belong to the specified shift plan");
            }
        }
        var invite = ShiftPlanInvite.builder()
            .code(code)
            .type(requestDto.getType())
            .shiftPlan(shiftPlan)
            .active(true)
            .expiresAt(requestDto.getExpiresAt())
            .maxUses(requestDto.getMaxUses())
            .uses(0)
            .createdAt(Instant.now())
            .autoAssignRoles(rolesToAssign.isEmpty() ? null : rolesToAssign)
            .build();
        // Should not happen but just in case we retry once
        try {
            shiftPlanInviteDao.save(invite);
        } catch (DataIntegrityViolationException e) {
            // fallback once, because code uniqueness might collide under concurrency
            invite.setCode(callGenerateUniqueCode());
            shiftPlanInviteDao.save(invite);
        }
        publisher.publishEvent(ShiftPlanInviteEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_CREATED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(invite.getId()))), invite));
        return ShiftPlanInviteCreateResponseDto.builder()
            .code(invite.getCode())
            .type(invite.getType())
            .expiresAt(invite.getExpiresAt())
            .maxUses(invite.getMaxUses())
            .build();
    }

    private String callGenerateUniqueCode() {
        return uniqueCodeGenerator.generateUnique(
            INVITE_CODE_ALPHABET,
            INVITE_CODE_LENGTH,
            MAX_INVITE_CODE_GENERATION_ATTEMPTS,
            shiftPlanInviteDao::existsByCode
        );
    }

    @Override
    public void revokeShiftPlanInvite(long inviteId) {
        var currentUser = userProvider.getCurrentUser();
        var invite = shiftPlanInviteDao.getById(inviteId);
        validatePermission(invite.getShiftPlan().getId(), invite.getType(), currentUser);
        invite.setActive(false);
        invite.setRevokedAt(Instant.now());
        publisher.publishEvent(ShiftPlanInviteEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_REVOKED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(inviteId))), invite));
        shiftPlanInviteDao.save(invite);
    }

    @Override
    public void deleteShiftPlanInvite(long inviteId) {
        var currentUser = userProvider.getCurrentUser();
        var invite = shiftPlanInviteDao.getById(inviteId);
        validatePermission(invite.getShiftPlan().getId(), invite.getType(), currentUser);
        ShiftPlanInviteEvent inviteEvent = ShiftPlanInviteEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_DELETED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(inviteId))), invite);
        shiftPlanInviteDao.delete(invite);
        publisher.publishEvent(inviteEvent);
    }

    private void validatePermission(long shiftPlanId, ShiftPlanInviteType type, ShiftControlUser currentUser) {
        if (type == ShiftPlanInviteType.VOLUNTEER_JOIN) {
            securityHelper.assertUserIsPlanner(shiftPlanId, currentUser);
        }
        boolean isNotAdmin = securityHelper.isNotUserAdmin(currentUser);
        // only allowed by admins
        if (type == ShiftPlanInviteType.PLANNER_JOIN && isNotAdmin) {
            throw new ForbiddenException("Only admins can create planner join invite codes.");
        }
    }

    @Override
    public ShiftPlanInviteDetailsDto getShiftPlanInviteDetails(String inviteCode) {
        var userId = userProvider.getCurrentUser().getUserId();
        var invite = shiftPlanInviteDao.getByCode(inviteCode);
        var shiftPlan = shiftPlanDao.getById(invite.getShiftPlan().getId());

        // volunteer not necessary to get invite details
        Volunteer volunteer = volunteerDao.findById(userId).orElse(null);

        boolean alreadyJoined = volunteer != null && isUserAlreadyInShiftPlan(invite.getType(), shiftPlan, volunteer);
        boolean upgradeToPlannerPossible = volunteer != null && isUserAlreadyInShiftPlan(ShiftPlanInviteType.VOLUNTEER_JOIN, shiftPlan, volunteer)
            && !isUserAlreadyInShiftPlan(ShiftPlanInviteType.PLANNER_JOIN, shiftPlan, volunteer)
            && invite.getType() == ShiftPlanInviteType.PLANNER_JOIN;

        var rolesToAssign = invite.getAutoAssignRoles();
        boolean extensionOfRolesPossible = volunteer != null
            && rolesToAssign != null
            && rolesToAssign.stream().anyMatch(role -> !volunteer.getRoles().contains(role));

        var eventDto = EventMapper.toEventDto(shiftPlan.getEvent());
        var inviteDto = InviteMapper.toInviteDto(invite, shiftPlan);
        return ShiftPlanInviteDetailsDto.builder()
            .attendingVolunteerCount(shiftPlan.getPlanVolunteers().size())
            .joined(alreadyJoined)
            .upgradeToPlannerPossible(upgradeToPlannerPossible)
            .extensionOfRolesPossible(extensionOfRolesPossible)
            .inviteDto(inviteDto)
            .eventDto(eventDto)
            .build();
    }

    private boolean isUserAlreadyInShiftPlan(ShiftPlanInviteType type, ShiftPlan shiftPlan, Volunteer volunteer) {
        switch (type) {
            case VOLUNTEER_JOIN -> {
                return shiftPlan.getPlanVolunteers().contains(volunteer);
            }
            case PLANNER_JOIN -> {
                return shiftPlan.getPlanPlanners().contains(volunteer);
            }
            default -> throw new BadRequestException("Unknown invite type");
        }
    }

    @Override
    public Collection<ShiftPlanInviteDto> getAllShiftPlanInvites(long shiftPlanId) {
        // both planners and admins can list invites
        securityHelper.assertUserIsPlanner(shiftPlanId);
        var shiftPlan = shiftPlanDao.getById(shiftPlanId);
        var invites = shiftPlanInviteDao.findAllByShiftPlanId(shiftPlanId);
        return invites.stream()
            .map(invite -> InviteMapper.toInviteDto(invite, shiftPlan))
            .toList();
    }

    @Override
    @Transactional
    public void joinShiftPlan(ShiftPlanJoinRequestDto requestDto) {
        String userId = userProvider.getCurrentUser().getUserId();
        if (requestDto == null || requestDto.getInviteCode() == null || requestDto.getInviteCode().isBlank()) {
            throw new BadRequestException("Invite code is null or empty");
        }
        String inviteCode = requestDto.getInviteCode().trim();
        ShiftPlanInvite invite = shiftPlanInviteDao.getByCode(inviteCode);
        validateInvite(invite);
        ShiftPlan shiftPlan = shiftPlanDao.getById(invite.getShiftPlan().getId());

        // volunteer data might not yet exist
        Volunteer volunteer = volunteerDao.findById(userId).orElseGet(() -> {
            var newVolunteer = Volunteer.builder()
                .id(userId)
                .planningPlans(Collections.emptySet())
                .volunteeringPlans(Collections.emptySet())
                .roles(Collections.emptySet())
                .notificationAssignments(Collections.emptySet())
                .build();
            return volunteerDao.save(newVolunteer);
        });

        boolean joinedNow = addUserToShiftPlanIfAbsent(invite.getType(), shiftPlan, volunteer);

        var rolesToAssign = invite.getAutoAssignRoles();
        addRolesToUser(rolesToAssign, volunteer);

        // Increase uses and invalidate cache if joined now
        if (joinedNow) {
            userAttributeProvider.invalidateUserCache(userId);
            invite.setUses(invite.getUses() + 1);
        }
        // auto-deactivate if max uses reached
        if (invite.getMaxUses() != null && invite.getUses() >= invite.getMaxUses()) {
            invite.setActive(false);
            invite.setRevokedAt(Instant.now());
        }
        // save updates
        shiftPlanInviteDao.save(invite);
        shiftPlanDao.save(shiftPlan);

        var key = invite.getType() == ShiftPlanInviteType.VOLUNTEER_JOIN
            ? RoutingKeys.SHIFTPLAN_JOINED_VOLUNTEER
            : RoutingKeys.SHIFTPLAN_JOINED_PLANNER;
        publisher.publishEvent(ShiftPlanVolunteerEvent.of(RoutingKeys.format(key,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()),
                "volunteerId", userId)), shiftPlan, userId));
    }

    private void validateInvite(ShiftPlanInvite invite) {
        if (!invite.isActive()) {
            throw new BadRequestException("Invite code is revoked or inactive");
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invite code is expired");
        }
        if (invite.getMaxUses() != null && invite.getUses() >= invite.getMaxUses()) {
            throw new BadRequestException("Invite code has reached its max uses");
        }
    }

    // returns true if user was newly added, false if already a member
    private boolean addUserToShiftPlanIfAbsent(ShiftPlanInviteType type, ShiftPlan shiftPlan, Volunteer volunteer) {
        switch (type) {
            case VOLUNTEER_JOIN -> {
                if (shiftPlan.getPlanVolunteers().contains(volunteer)) {
                    return false;
                }
                shiftPlan.addPlanVolunteer(volunteer);
                return true;
            }
            case PLANNER_JOIN -> {
                if (shiftPlan.getPlanPlanners().contains(volunteer)) {
                    return false;
                }
                shiftPlan.addPlanPlanner(volunteer);
                if (!shiftPlan.getPlanVolunteers().contains(volunteer)) {
                    shiftPlan.addPlanVolunteer(volunteer); // planners are also volunteers (if not already)
                }
                return true;
            }
            default -> throw new BadRequestException("Unknown invite type");
        }
    }

    private void addRolesToUser(Collection<Role> rolesToAssign, Volunteer volunteer) {
        if (rolesToAssign != null && !rolesToAssign.isEmpty()) {
            for (var role : rolesToAssign) {
                if (!volunteer.getRoles().contains(role)) {
                    volunteer.getRoles().add(role);
                }
            }
            volunteerDao.save(volunteer);
        }
    }

    @Override
    public void updateLockStatus(long shiftPlanId, LockStatus lockStatus) {
        var shiftPlan = shiftPlanDao.getById(shiftPlanId);
        if (shiftPlan.getLockStatus().equals(lockStatus)) {
            throw new BadRequestException("Lock status already in requested state");
        }
        if (shiftPlan.getLockStatus().equals(LockStatus.SUPERVISED)
            && lockStatus.equals(LockStatus.SELF_SIGNUP)) {
            assignmentService.unassignAllAuctions(shiftPlan);
        }
        shiftPlan.setLockStatus(lockStatus);
        publisher.publishEvent(ShiftPlanEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_LOCKSTATUS_CHANGED,
            Map.of("shiftPlanId", String.valueOf(shiftPlanId))), shiftPlan));
        shiftPlanDao.save(shiftPlan);
    }
}
