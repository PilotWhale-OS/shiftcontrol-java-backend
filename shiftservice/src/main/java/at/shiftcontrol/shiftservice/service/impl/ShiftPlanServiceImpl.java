package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanInviteDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.LocationScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftColumnDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final StatisticService statisticService;
    private final EligibilityService eligibilityService;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftPlanInviteDao shiftPlanInviteDao;
    private final ShiftDao shiftDao;
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationUserProvider userProvider;

    @Override
    public DashboardOverviewDto getDashboardOverview(long shiftPlanId) throws NotFoundException, ForbiddenException {
        var userId = validateShiftPlanAccessAndGetUserId(shiftPlanId);
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var event = eventDao.findById(shiftPlan.getEvent().getId())
            .orElseThrow(() -> new NotFoundException("Event of shift plan with id " + shiftPlanId + " not found"));

        var userShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId);
        var volunteer = volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));

        return DashboardOverviewDto.builder()
            .shiftPlan(ShiftPlanMapper.toShiftPlanDto(shiftPlan))
            .eventOverview(EventMapper.toEventOverviewDto(event))
            .ownShiftPlanStatistics(statisticService.getOwnShiftPlanStatistics(userShifts)) // directly pass user shifts here to avoid redundant filtering
            .overallShiftPlanStatistics(statisticService.getOverallShiftPlanStatistics(shiftPlanId))
            .rewardPoints(-1) // TODO
            .shifts(ShiftMapper.toShiftDto(userShifts, slot -> eligibilityService.getSignupStateForPositionSlot(slot, volunteer)))
            .trades(null) // TODO implement when trades are available
            .auctions(null) // TODO
            .build();
    }

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) throws NotFoundException {
        return shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));
    }

    @Override
    public ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException, ForbiddenException {
        var userId = validateShiftPlanAccessAndGetUserId(shiftPlanId);
        var queriedShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId, searchDto);

        var volunteer = volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));

        // Get user related shifts via dao if MY_SHIFTS view is requested
        // Get shifts with signup possible if SIGNUP_POSSIBLE view is requested; This filtering is done here because it depends on business logic
        // and it wouldn't make sense to implement this existing logic in the DAO layer (which would be quite complex)
        if (searchDto != null && searchDto.getScheduleViewType() == ScheduleViewType.SIGNUP_POSSIBLE) {
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

        // Build location DTOs
        var locationSchedules = shiftsByLocation.entrySet().stream()
            .map(entry -> buildLocationSchedule(entry.getKey(), entry.getValue(), volunteer))
            .toList();

        var stats = statisticService.getShiftPlanScheduleStatistics(queriedShifts);

        return ShiftPlanScheduleDto.builder()
            .date(searchDto != null ? searchDto.getDate() : null)
            .locations(locationSchedules)
            .scheduleStatistics(stats)
            .build();
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


    private LocationScheduleDto buildLocationSchedule(Location location, List<Shift> shifts, Volunteer volunteer) {
        // sort for deterministic column placement
        shifts.sort(Comparator
            .comparing(Shift::getStartTime)
            .thenComparing(Shift::getEndTime));

        var shiftColumns = calculateShiftColumns(shifts, volunteer);
        int requiredShiftColumns = shiftColumns.stream()
            .mapToInt(ShiftColumnDto::getColumnIndex)
            .max()
            .orElse(-1) + 1;

        var activities = shifts.stream()
            .flatMap(s -> s.getRelatedActivities() == null ? Stream.empty() : s.getRelatedActivities().stream())
            .distinct()
            .map(ActivityMapper::toActivityDto)
            .toList();

        return LocationScheduleDto.builder()
            .location(LocationMapper.toLocationDto(location))
            .activities(activities)
            .requiredShiftColumns(requiredShiftColumns)
            .shiftColumns(shiftColumns)
            .build();
    }

    private List<ShiftColumnDto> calculateShiftColumns(List<Shift> sortedShifts, Volunteer volunteer) {
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
                .shiftDto(ShiftMapper.toShiftDto(shift, slot -> eligibilityService.getSignupStateForPositionSlot(slot, volunteer)))
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
    @Transactional
    public ShiftPlanJoinOverviewDto joinShiftPlan(long shiftPlanId, ShiftPlanJoinRequestDto requestDto) throws NotFoundException {
        String userId = userProvider.getCurrentUser().getUserId();
        if (requestDto == null || requestDto.getInviteCode() == null || requestDto.getInviteCode().isBlank()) {
            throw new BadRequestException("inviteCode is null or empty");
        }

        String code = requestDto.getInviteCode().trim();

        ShiftPlanInvite invite = shiftPlanInviteDao.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Invite code not found"));

        validateInvite(invite);

        // Make sure invite belongs to the requested shiftPlanId
        long invitedShiftPlanId = invite.getShiftPlan().getId();
        if (invitedShiftPlanId != shiftPlanId) {
            throw new BadRequestException("Invite code does not belong to this shift plan");
        }

        ShiftPlan shiftPlan = shiftPlanDao.findById(shiftPlanId)
            .orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));

        Volunteer volunteer = volunteerDao.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));

        boolean joinedNow = addUserToShiftPlanIfAbsent(invite.getType(), shiftPlan, volunteer);

        // Increase uses only if joined now and ignores duplicate joins (already member)
        if (joinedNow) {
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

        return ShiftPlanJoinOverviewDto.builder()
            .shiftPlanDto(ShiftPlanMapper.toShiftPlanDto(shiftPlan))
            .attendingVolunteerCount(shiftPlan.getPlanVolunteers().size())
            .inviteType(invite.getType())
            .joined(joinedNow)
            .build();
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
                // TODO implement planner membership
                throw new BadRequestException("Planner invites not supported yet");
            }
            default -> throw new BadRequestException("Unknown invite type");
        }
    }
}
