package at.shiftcontrol.shiftservice.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanInviteDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftColumnDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleLayoutDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleLayoutDto;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.InviteMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.type.LockStatus;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final StatisticService statisticService;
    private final EligibilityService eligibilityService;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftPlanInviteDao shiftPlanInviteDao;
    private final ShiftDao shiftDao;
    private final ActivityDao activityDao;
    private final RoleDao roleDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationUserProvider userProvider;
    private final ShiftAssemblingMapper shiftMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    // URL-safe, human-friendly alphabet (no 0/O, 1/I to reduce confusion)
    private static final String INVITE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 10;

    @Override
    public ShiftPlanScheduleLayoutDto getShiftPlanScheduleLayout(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto)
        throws NotFoundException, ForbiddenException {
        var shiftsByLocation = getScheduleShiftsByLocation(shiftPlanId, filterDto);

        // Build location DTOs
        var scheduleLayoutDtos = shiftsByLocation.entrySet().stream()
            .map(entry -> buildScheduleLayoutDto(entry.getKey(), entry.getValue()))
            .toList();

        return ShiftPlanScheduleLayoutDto.builder()
            .scheduleLayoutDtos(scheduleLayoutDtos)
            .build();
    }

    private ScheduleLayoutDto buildScheduleLayoutDto(Location location, List<Shift> shifts) {
        // sort for deterministic column placement
        shifts.sort(Comparator
            .comparing(Shift::getStartTime)
            .thenComparing(Shift::getEndTime));

        var shiftColumns = calculateShiftColumns(shifts);

        int requiredShiftColumns = shiftColumns.stream()
            .mapToInt(ShiftColumnDto::getColumnIndex)
            .max()
            .orElse(-1) + 1;

        return ScheduleLayoutDto.builder()
            .location(LocationMapper.toLocationDto(location))
            .requiredShiftColumns(requiredShiftColumns)
            .build();
    }

    @Override
    public ShiftPlanScheduleContentDto getShiftPlanScheduleContent(long shiftPlanId, ShiftPlanScheduleDaySearchDto searchDto)
        throws NotFoundException, ForbiddenException {
        var shiftsByLocation = getScheduleShiftsByLocation(shiftPlanId, searchDto);

        // Build location DTOs
        var scheduleContentDtos = shiftsByLocation.entrySet().stream()
            .map(entry -> buildScheduleContentDto(entry.getKey(), entry.getValue()))
            .toList();

        var stats = statisticService.getShiftPlanScheduleStatistics(shiftsByLocation.values().stream().flatMap(List::stream).toList());

        return ShiftPlanScheduleContentDto.builder()
            .date(searchDto != null ? searchDto.getDate() : null)
            .scheduleContentDtos(scheduleContentDtos)
            .scheduleStatistics(stats)
            .build();
    }

    private Map<Location, List<Shift>> getScheduleShiftsByLocation(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto)
        throws ForbiddenException, NotFoundException {
        var userId = validateShiftPlanAccessAndGetUserId(shiftPlanId);
        var queriedShifts = shiftDao.searchShiftsInShiftPlan(shiftPlanId, userId, filterDto);

        var volunteer = volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));

        // Get user related shifts via dao if MY_SHIFTS view is requested
        // Get shifts with signup possible if SIGNUP_POSSIBLE view is requested; This filtering is done here because it depends on business logic
        // and it wouldn't make sense to implement this existing logic in the DAO layer (which would be quite complex)
        if (filterDto != null && filterDto.getScheduleViewType() == ScheduleViewType.SIGNUP_POSSIBLE) {
            queriedShifts = queriedShifts.stream()
                .filter(shift -> shift.getSlots().stream().anyMatch(slot ->
                    isSignupPossible(slot, volunteer)
                ))
                .toList();
        }

        Map<Location, List<Shift>> shiftsByLocation = new HashMap<>();
        for (var shift : queriedShifts) {
            if (shift.getLocation() == null) {
                continue;
            }
            shiftsByLocation.computeIfAbsent(shift.getLocation(), k -> new ArrayList<>()).add(shift);
        }

        return shiftsByLocation;
    }

    private String validateShiftPlanAccessAndGetUserId(long shiftPlanId) throws ForbiddenException {
        var currentUser = userProvider.getCurrentUser();
        if (!(currentUser.isVolunteerInPlan(shiftPlanId) || currentUser.isPlannerInPlan(shiftPlanId))) {
            throw new ForbiddenException("User has no access to shift plan with id: " + shiftPlanId);
        }

        return currentUser.getUserId();
    }

    private boolean isSignupPossible(PositionSlot slot, Volunteer volunteer) {
        var state = eligibilityService.getSignupStateForPositionSlot(slot, volunteer);

        // no further actions needed if not eligible
        if (state == PositionSignupState.NOT_ELIGIBLE) {
            return false;
        }

        boolean freeAndEligible = state == PositionSignupState.SIGNUP_POSSIBLE;

        boolean hasOpenTrade = state == PositionSignupState.SIGNUP_VIA_TRADE;

        boolean hasAuction = state == PositionSignupState.SIGNUP_VIA_AUCTION;

        return freeAndEligible || hasOpenTrade || hasAuction;
    }


    private ScheduleContentDto buildScheduleContentDto(Location location, List<Shift> shifts) {
        // sort for deterministic column placement
        shifts.sort(Comparator
            .comparing(Shift::getStartTime)
            .thenComparing(Shift::getEndTime));

        var shiftColumns = calculateShiftColumns(shifts);

        // get activities related to this location (shift unrelated, even when no shifts are present)
        var activitiesRelatedToLocation = activityDao.findAllByLocationId(location.getId()).stream()
            .distinct()
            .map(ActivityMapper::toActivityDto)
            .toList();

        return ScheduleContentDto.builder()
            .location(LocationMapper.toLocationDto(location))
            .activities(activitiesRelatedToLocation)
            .shiftColumns(shiftColumns)
            .build();
    }

    private List<ShiftColumnDto> calculateShiftColumns(List<Shift> sortedShifts) {
        // end time per column
        var columnEndTimes = new ArrayList<Instant>();
        var result = new ArrayList<ShiftColumnDto>(sortedShifts.size());

        for (var shift : sortedShifts) {
            int columnIndex = findFirstFreeColumnIndex(columnEndTimes, shift.getStartTime());
            if (columnIndex == -1) {
                columnIndex = columnEndTimes.size();
                columnEndTimes.add(shift.getEndTime());
            } else {
                columnEndTimes.set(columnIndex, shift.getEndTime());
            }

            result.add(ShiftColumnDto.builder()
                .columnIndex(columnIndex)
                .shiftDto(shiftMapper.assemble(shift))
                .build());
        }

        return result;
    }

    private int findFirstFreeColumnIndex(List<Instant> columnEndTimes, Instant startTime) {
        for (int i = 0; i < columnEndTimes.size(); i++) {
            // column is free if previous shift ended at or before this start
            if (!columnEndTimes.get(i).isAfter(startTime)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId) throws NotFoundException {
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);

        var shifts = shiftPlan.getShifts();

        if (shifts == null || shifts.isEmpty()) {
            return ShiftPlanScheduleFilterValuesDto.builder()
                .build();
        }

        var locations = shifts.stream()
            .map(Shift::getLocation)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        var roles = shifts.stream()
            .flatMap(shift -> shift.getSlots().stream())
            .map(PositionSlot::getRole)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // Determine first and last date from shifts and related activities
        var firstDate = Stream.concat(
                shifts.stream().map(Shift::getStartTime),
                shifts.stream()
                    .map(Shift::getRelatedActivity)
                    .filter(Objects::nonNull)
                    .map(Activity::getStartTime)
            )
            .filter(Objects::nonNull)
            .min(Instant::compareTo)
            .map(TimeUtil::convertToUtcLocalDate)
            .orElse(null);


        var lastDate = Stream.concat(
                shifts.stream().map(Shift::getEndTime),
                shifts.stream()
                    .map(Shift::getRelatedActivity)
                    .filter(Objects::nonNull)
                    .map(Activity::getEndTime)
            )
            .filter(Objects::nonNull)
            .max(Instant::compareTo)
            .map(TimeUtil::convertToUtcLocalDate)
            .orElse(null);

        return ShiftPlanScheduleFilterValuesDto.builder()
            .locations(locations.isEmpty() ? null : LocationMapper.toLocationDto(locations))
            .roles(roles.isEmpty() ? null : RoleMapper.toRoleDto(roles))
            .firstDate(firstDate)
            .lastDate(lastDate)
            .build();
    }

    @Override
    public ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto)
        throws NotFoundException, ForbiddenException {
        var currentUser = userProvider.getCurrentUser();

        validatePermission(shiftPlanId, requestDto.getType(), currentUser);

        final var shiftPlan = getShiftPlanOrThrow(shiftPlanId);

        if (requestDto.getExpiresAt() != null && requestDto.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("expiresAt must be in the future");
        }
        if (requestDto.getMaxUses() != null && requestDto.getMaxUses() <= 0) {
            throw new BadRequestException("maxUses must be positive");
        }

        // Generate a unique code (retry a few times)
        String code = generateUniqueCode();

        Collection<Role> rolesToAssign = List.of();
        if (requestDto.getAutoAssignRoleIds() != null && !requestDto.getAutoAssignRoleIds().isEmpty()) {
            rolesToAssign = roleDao.findAllById(requestDto.getAutoAssignRoleIds());

            if (rolesToAssign.size() != requestDto.getAutoAssignRoleIds().size()) {
                throw new BadRequestException("One or more roleIds are invalid");
            }

            // TODO verify roles belong to this shiftPlan
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
            invite.setCode(generateUniqueCode());
            shiftPlanInviteDao.save(invite);
        }

        return ShiftPlanInviteCreateResponseDto.builder()
            .code(invite.getCode())
            .type(invite.getType())
            .expiresAt(invite.getExpiresAt())
            .maxUses(invite.getMaxUses())
            .build();
    }

    private String generateUniqueCode() {
        final char[] alphabet = INVITE_CODE_ALPHABET.toCharArray();

        for (int attempt = 0; attempt < MAX_INVITE_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = randomString(alphabet);
            if (!shiftPlanInviteDao.existsByCode(code)) {
                return code;
            }
        }
        // extremely unlikely unless length is too short / huge volume
        throw new RuntimeException("Could not generate unique invite code");
    }

    private String randomString(char[] alphabet) {
        var sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            int idx = secureRandom.nextInt(alphabet.length);
            sb.append(alphabet[idx]);
        }
        return sb.toString();
    }

    @Override
    public void revokeShiftPlanInviteCode(String inviteCode) throws NotFoundException, ForbiddenException {
        var currentUser = userProvider.getCurrentUser();

        var invite = shiftPlanInviteDao.findByCode(inviteCode)
            .orElseThrow(() -> new NotFoundException("Invite code not found: " + inviteCode));

        validatePermission(invite.getShiftPlan().getId(), invite.getType(), currentUser);

        invite.setActive(false);
        invite.setRevokedAt(Instant.now());

        shiftPlanInviteDao.save(invite);
    }

    private void validatePermission(long shiftPlanId, ShiftPlanInviteType type, ShiftControlUser currentUser) throws ForbiddenException {
        if (type == ShiftPlanInviteType.VOLUNTEER_JOIN && !currentUser.isPlannerInPlan(shiftPlanId)) {
            throw new ForbiddenException("User is not a planner in shift plan with id: " + shiftPlanId);
        }

        // only allowed by admins
        if (type == ShiftPlanInviteType.PLANNER_JOIN && !(currentUser instanceof AdminUser)) {
            throw new ForbiddenException("Only admins can create planner join invite codes");
        }
    }

    @Override
    public ShiftPlanJoinOverviewDto getShiftPlanInviteDetails(String inviteCode) throws NotFoundException, ForbiddenException {
        var userId = userProvider.getCurrentUser().getUserId();
        var invite = shiftPlanInviteDao.findByCode(inviteCode)
            .orElseThrow(() -> new NotFoundException("Invite code not found: " + inviteCode));

        var shiftPlan = getShiftPlanOrThrow(invite.getShiftPlan().getId());
        var volunteer = volunteerDao.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));
        boolean alreadyJoined = userIsInShiftPlan(invite.getType(), shiftPlan, volunteer);

        var eventDto = EventMapper.toEventOverviewDto(shiftPlan.getEvent());
        var inviteDto = InviteMapper.toInviteDto(invite, shiftPlan);

        return ShiftPlanJoinOverviewDto.builder()
            .attendingVolunteerCount(shiftPlan.getPlanVolunteers().size())
            .joined(alreadyJoined)
            .inviteDto(inviteDto)
            .eventDto(eventDto)
            .build();
    }

    @Override
    public Collection<ShiftPlanInviteDto> listShiftPlanInvites(long shiftPlanId) throws NotFoundException, ForbiddenException {
        var currentUser = userProvider.getCurrentUser();

        // both planners and admins can list invites
        if (!currentUser.isPlannerInPlan(shiftPlanId)) {
            throw new ForbiddenException("User is not a planner in shift plan with id: " + shiftPlanId);
        }

        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);

        var invites = shiftPlanInviteDao.findAllByShiftPlanId(shiftPlanId);

        return invites.stream()
            .map(invite -> InviteMapper.toInviteDto(invite, shiftPlan))
            .toList();
    }

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) throws NotFoundException {
        return shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));
    }

    @Override
    @Transactional
    public ShiftPlanJoinOverviewDto joinShiftPlanAsVolunteer(ShiftPlanJoinRequestDto requestDto) throws NotFoundException {
        String userId = userProvider.getCurrentUser().getUserId();
        if (requestDto == null || requestDto.getInviteCode() == null || requestDto.getInviteCode().isBlank()) {
            throw new BadRequestException("inviteCode is null or empty");
        }

        String code = requestDto.getInviteCode().trim();

        ShiftPlanInvite invite = shiftPlanInviteDao.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Invite code not found"));

        validateInvite(invite);
        ShiftPlan shiftPlan = getShiftPlanOrThrow(invite.getShiftPlan().getId());

        Volunteer volunteer = volunteerDao.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));

        boolean joinedNow = addUserToShiftPlanIfAbsent(invite.getType(), shiftPlan, volunteer);

        // Increase uses and add roles only if joined now and ignores duplicate joins (already member)
        if (joinedNow) {
            var rolesToAssign = invite.getAutoAssignRoles();
            if (rolesToAssign != null && !rolesToAssign.isEmpty()) {
                for (var role : rolesToAssign) {
                    if (!volunteer.getRoles().contains(role)) {
                        volunteer.getRoles().add(role);
                    }
                }
                volunteerDao.save(volunteer);
            }

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


        var eventDto =EventMapper.toEventOverviewDto(shiftPlan.getEvent());
        var inviteDto = InviteMapper.toInviteDto(invite, shiftPlan);

        return ShiftPlanJoinOverviewDto.builder()
            .attendingVolunteerCount(shiftPlan.getPlanVolunteers().size())
            .joined(joinedNow)
            .inviteDto(inviteDto)
            .eventDto(eventDto)
            .build();
    }

    @Override
    public void updateLockStatus(long shiftPlanId, LockStatus lockStatus) throws NotFoundException {
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        if (shiftPlan.getLockStatus().equals(lockStatus)) {
            throw new BadRequestException("Lock status already in requested state");
        }
        shiftPlan.setLockStatus(lockStatus);
        shiftPlanDao.save(shiftPlan);
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
                shiftPlan.getPlanVolunteers().add(volunteer);
                // TODO add plan to volunteeringPlans too ?? Is this needed or resolved by ManyToMany mapping ??
                return true;
            }
            case PLANNER_JOIN -> {
                if (shiftPlan.getPlanPlanners().contains(volunteer)) {
                    return false;
                }
                shiftPlan.getPlanPlanners().add(volunteer);
                return true;
            }
            default -> throw new BadRequestException("Unknown invite type");
        }
    }

    private boolean userIsInShiftPlan(ShiftPlanInviteType type, ShiftPlan shiftPlan, Volunteer volunteer) {
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
}
