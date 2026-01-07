package at.shiftcontrol.shiftservice.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanInviteDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftColumnDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleContentNoLocationDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleLayoutDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanModificationDto;
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
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.ShiftPlanEvent;
import at.shiftcontrol.shiftservice.event.events.ShiftPlanInviteEvent;
import at.shiftcontrol.shiftservice.event.events.ShiftPlanVolunteerEvent;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.InviteMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.type.LockStatus;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;
import at.shiftcontrol.shiftservice.type.ShiftRelevance;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final StatisticService statisticService;
    private final EligibilityService eligibilityService;
    private final AssignmentService assignmentService;

    private final EventDao eventDao;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftPlanInviteDao shiftPlanInviteDao;
    private final ShiftDao shiftDao;
    private final ActivityDao activityDao;
    private final RoleDao roleDao;
    private final VolunteerDao volunteerDao;

    private final ShiftAssemblingMapper shiftMapper;
    private final SecurityHelper securityHelper;
    private final ApplicationUserProvider userProvider;
    private final ApplicationEventPublisher publisher;
    private final UserAttributeProvider userAttributeProvider;

    private final SecureRandom secureRandom = new SecureRandom();

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
    public ShiftPlanDto createShiftPlan(long eventId, ShiftPlanModificationDto modificationDto) {
        var event = eventDao.getById(eventId);
        var plan = ShiftPlanMapper.toShiftPlan(modificationDto);
        plan.setEvent(event);
        plan.setLockStatus(LockStatus.SELF_SIGNUP);
        plan = shiftPlanDao.save(plan);
        publisher.publishEvent(ShiftPlanEvent.of(RoutingKeys.SHIFTPLAN_CREATED, plan));
        return ShiftPlanMapper.toShiftPlanDto(plan);
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
        shiftPlanDao.delete(shiftPlan);
        publisher.publishEvent(ShiftPlanEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_DELETED,
            Map.of("shiftPlanId", String.valueOf(shiftPlanId))), shiftPlan));
    }

    @Override
    public ShiftPlanScheduleLayoutDto getShiftPlanScheduleLayout(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto) {
        var shiftsByLocation = getScheduleShiftsByLocation(shiftPlanId, filterDto);
        // Build location DTOs
        var scheduleLayoutDtos = shiftsByLocation.entrySet().stream()
            .map(entry -> buildScheduleLayoutDto(entry.getKey(), entry.getValue()))
            .toList();
        var stats = statisticService.getShiftPlanScheduleStatistics(shiftsByLocation.values().stream().flatMap(List::stream).toList());
        return ShiftPlanScheduleLayoutDto.builder()
            .scheduleLayoutDtos(scheduleLayoutDtos)
            .scheduleStatistics(stats)
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
    public ShiftPlanScheduleContentDto getShiftPlanScheduleContent(long shiftPlanId, ShiftPlanScheduleDaySearchDto searchDto) {
        var shiftsByLocation = getScheduleShiftsByLocation(shiftPlanId, searchDto);
        // Build location DTOs
        var scheduleContentDtos = shiftsByLocation.entrySet().stream()
            .map(entry -> buildScheduleContentDto(entry.getKey(), entry.getValue()))
            .toList();

        var scheduleContentNoLocationDto = buildScheduleContentNoLocationDto(shiftPlanId, searchDto);

        return ShiftPlanScheduleContentDto.builder()
            .date(searchDto != null ? searchDto.getDate() : null)
            .scheduleContentDtos(scheduleContentDtos)
            .scheduleContentNoLocationDto(scheduleContentNoLocationDto)
            .build();
    }

    private Map<Location, List<Shift>> getScheduleShiftsByLocation(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto) {
        var userId = validateShiftPlanAccessAndGetUserId(shiftPlanId);
        // if param is ShiftPlanScheduleFilterDto filtering is done without date; date filtering is only done if param is ShiftPlanScheduleDaySearchDto instance
        var filteredShiftsWithoutViewMode = shiftDao.searchShiftsInShiftPlan(shiftPlanId, userId, filterDto);
        var queriedShifts = getShiftsBasedOnViewModes(shiftPlanId, userId, filterDto, filteredShiftsWithoutViewMode);
        Map<Location, List<Shift>> shiftsByLocation = new HashMap<>();
        for (var shift : queriedShifts) {
            if (shift.getLocation() == null) {
                continue;
            }
            shiftsByLocation.computeIfAbsent(shift.getLocation(), k -> new ArrayList<>()).add(shift);
        }
        // add all other locations of the event without shifts
        var locations = shiftPlanDao.getById(shiftPlanId).getEvent().getLocations();
        for (var location : locations) {
            shiftsByLocation.putIfAbsent(location, new ArrayList<>());
        }

        return shiftsByLocation;
    }

    private String validateShiftPlanAccessAndGetUserId(long shiftPlanId) {
        securityHelper.assertUserIsInPlan(shiftPlanId);
        return userProvider.getCurrentUser().getUserId();
    }

    private boolean isSignupPossible(PositionSlot slot, Volunteer volunteer) {
        var state = eligibilityService.getSignupStateForPositionSlot(slot, volunteer);
        // no further actions needed if not eligible
        if (state == PositionSignupState.NOT_ELIGIBLE) {
            return false;
        }
        boolean freeAndOpenTrade = state == PositionSignupState.SIGNUP_OR_TRADE;
        boolean freeAndEligible = state == PositionSignupState.SIGNUP_POSSIBLE;
        boolean hasOpenTrade = state == PositionSignupState.SIGNUP_VIA_TRADE;
        boolean hasAuction = state == PositionSignupState.SIGNUP_VIA_AUCTION;
        return freeAndEligible || freeAndOpenTrade || hasOpenTrade || hasAuction;
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

    private ScheduleContentNoLocationDto buildScheduleContentNoLocationDto(
        long shiftPlanId,
        ShiftPlanScheduleDaySearchDto searchDto) {

        // get activities without location
        var activitiesWithoutLocation = activityDao.findAllWithoutLocationByShiftPlanId(shiftPlanId).stream()
            .distinct()
            .map(ActivityMapper::toActivityDto)
            .toList();

        // get shifts without location
        var shiftsWithoutLocation = shiftDao.searchShiftsInShiftPlan(shiftPlanId,
                userProvider.getCurrentUser().getUserId(),
                searchDto).stream()
            .filter(shift -> shift.getLocation() == null)
            .toList();

        var filteredShiftsWithoutLocation = getShiftsBasedOnViewModes(shiftPlanId,
            userProvider.getCurrentUser().getUserId(),
            searchDto,
            shiftsWithoutLocation);

        var shiftColumns = calculateShiftColumns(filteredShiftsWithoutLocation);

        return ScheduleContentNoLocationDto.builder()
            .activities(activitiesWithoutLocation)
            .shiftColumns(shiftColumns)
            .build();
    }

    private List<Shift> getShiftsBasedOnViewModes(
        long shiftPlanId,
        String userId,
        ShiftPlanScheduleFilterDto filterDto,
        List<Shift> filteredShiftsWithoutViewMode) {
        if (filterDto != null
            && filterDto.getShiftRelevances() != null
            && !filterDto.getShiftRelevances().isEmpty()) {
            List<Shift> ownShifts = new ArrayList<>();
            List<Shift> signUpPossibleShifts = new ArrayList<>();
            if (filterDto.getShiftRelevances().contains(ShiftRelevance.MY_SHIFTS)) {
                ownShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId);
            }
            if (filterDto.getShiftRelevances().contains(ShiftRelevance.SIGNUP_POSSIBLE)) {
                signUpPossibleShifts = new ArrayList<>(filteredShiftsWithoutViewMode);
                var volunteer = volunteerDao.getById(userId);
                signUpPossibleShifts = signUpPossibleShifts.stream()
                    .filter(shift -> shift.getSlots().stream().anyMatch(slot ->
                        isSignupPossible(slot, volunteer)
                    ))
                    .toList();
            }
            // combine results
            var combinedShifts = new ArrayList<>(ownShifts);
            for (var shift : signUpPossibleShifts) {
                if (!combinedShifts.contains(shift)) {
                    combinedShifts.add(shift);
                }
            }
            // combine combined shifts with filteredShiftsWithoutViewMode to apply other filters
            return combinedShifts.stream()
                .filter(filteredShiftsWithoutViewMode::contains)
                .toList();
        }
        return filteredShiftsWithoutViewMode;
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
    public ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId) {
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var shifts = shiftPlan.getShifts();
        if (shifts == null || shifts.isEmpty()) {
            return ShiftPlanScheduleFilterValuesDto.builder()
                .locations(List.of())
                .roles(List.of())
                .firstDate(TimeUtil.convertToUtcLocalDate(shiftPlan.getEvent().getStartTime()))
                .lastDate(TimeUtil.convertToUtcLocalDate(shiftPlan.getEvent().getEndTime()))
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
            .orElse(TimeUtil.convertToUtcLocalDate(shiftPlan.getEvent().getStartTime()));
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
            .orElse(TimeUtil.convertToUtcLocalDate(shiftPlan.getEvent().getEndTime()));

        return ShiftPlanScheduleFilterValuesDto.builder()
            .locations(locations.isEmpty() ? List.of() : LocationMapper.toLocationDto(locations))
            .roles(roles.isEmpty() ? List.of() : RoleMapper.toRoleDto(roles))
            .firstDate(firstDate)
            .lastDate(lastDate)
            .build();
    }

    @Override
    public ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto) {
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
            invite.setCode(generateUniqueCode());
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
        shiftPlanInviteDao.delete(invite);
        publisher.publishEvent(ShiftPlanInviteEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_DELETED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(inviteId))), invite));
    }

    private void validatePermission(long shiftPlanId, ShiftPlanInviteType type, ShiftControlUser currentUser) {
        if (type == ShiftPlanInviteType.VOLUNTEER_JOIN) {
            securityHelper.assertUserIsPlanner(shiftPlanId, currentUser);
        }
        boolean isNotAdmin = securityHelper.isNotUserAdmin(currentUser);
        // only allowed by admins
        if (type == ShiftPlanInviteType.PLANNER_JOIN && isNotAdmin) {
            throw new ForbiddenException("Only admins can create planner join invite codes");
        }
    }

    @Override
    public ShiftPlanJoinOverviewDto getShiftPlanInviteDetails(String inviteCode) {
        var userId = userProvider.getCurrentUser().getUserId();
        var invite = shiftPlanInviteDao.getByCode(inviteCode);
        var shiftPlan = getShiftPlanOrThrow(invite.getShiftPlan().getId());

        /* volunteer not necessary to get invite details */
        Volunteer volunteer;
        try {
            volunteer = volunteerDao.getById(userId);
        } catch (NotFoundException e) {
            volunteer = null;
        }

        boolean alreadyJoined = (volunteer != null) && userIsInShiftPlan(invite.getType(), shiftPlan, volunteer);
        var eventDto = EventMapper.toEventDto(shiftPlan.getEvent());
        var inviteDto = InviteMapper.toInviteDto(invite, shiftPlan);
        return ShiftPlanJoinOverviewDto.builder()
            .attendingVolunteerCount(shiftPlan.getPlanVolunteers().size())
            .joined(alreadyJoined)
            .inviteDto(inviteDto)
            .eventDto(eventDto)
            .build();
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

    @Override
    public Collection<ShiftPlanInviteDto> getAllShiftPlanInvites(long shiftPlanId) {
        // both planners and admins can list invites
        securityHelper.assertUserIsPlanner(shiftPlanId);
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var invites = shiftPlanInviteDao.findAllByShiftPlanId(shiftPlanId);
        return invites.stream()
            .map(invite -> InviteMapper.toInviteDto(invite, shiftPlan))
            .toList();
    }

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) {
        return shiftPlanDao.getById(shiftPlanId);
    }

    @Override
    @Transactional
    public ShiftPlanJoinOverviewDto joinShiftPlan(ShiftPlanJoinRequestDto requestDto) {
        String userId = userProvider.getCurrentUser().getUserId();
        if (requestDto == null || requestDto.getInviteCode() == null || requestDto.getInviteCode().isBlank()) {
            throw new BadRequestException("Invite code is null or empty");
        }
        String inviteCode = requestDto.getInviteCode().trim();
        ShiftPlanInvite invite = shiftPlanInviteDao.getByCode(inviteCode);
        validateInvite(invite);
        ShiftPlan shiftPlan = getShiftPlanOrThrow(invite.getShiftPlan().getId());

        /* volunteer data might not yet exist */
        Volunteer volunteer;
        try {
            volunteer = volunteerDao.getById(userId);
        } catch (NotFoundException e) {
            volunteer = Volunteer.builder()
                .id(userId)
                .planningPlans(Collections.emptySet())
                .volunteeringPlans(Collections.emptySet())
                .roles(Collections.emptySet())
                .notificationAssignments(Collections.emptySet())
                .build();
            volunteerDao.save(volunteer);
        }

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
                userAttributeProvider.invalidateUserCache(userId);
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
        var eventDto = EventMapper.toEventDto(shiftPlan.getEvent());
        var inviteDto = InviteMapper.toInviteDto(invite, shiftPlan);

        publisher.publishEvent(ShiftPlanVolunteerEvent.of(RoutingKeys.format(RoutingKeys.SHIFTPLAN_JOINED_VOLUNTEER,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()),
                "volunteerId", userId)), shiftPlan, userId));

        return ShiftPlanJoinOverviewDto.builder()
            .attendingVolunteerCount(shiftPlan.getPlanVolunteers().size())
            .joined(joinedNow)
            .inviteDto(inviteDto)
            .eventDto(eventDto)
            .build();
    }

    @Override
    public void updateLockStatus(long shiftPlanId, LockStatus lockStatus) {
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
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
                return true;
            }
            case PLANNER_JOIN -> {
                if (shiftPlan.getPlanPlanners().contains(volunteer)) {
                    return false;
                }
                shiftPlan.getPlanPlanners().add(volunteer);
                shiftPlan.getPlanVolunteers().add(volunteer); // planners are also volunteers
                return true;
            }
            default -> throw new BadRequestException("Unknown invite type");
        }
    }
}
