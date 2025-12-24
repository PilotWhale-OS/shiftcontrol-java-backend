package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.LocationScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftColumnDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final StatisticService statisticService;
    private final EligibilityService eligibilityService;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftDao shiftDao;
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;
    private final ShiftAssemblingMapper shiftMapper;

    @Override
    public DashboardOverviewDto getDashboardOverview(long shiftPlanId, String userId) throws NotFoundException {
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var event = eventDao.findById(shiftPlan.getEvent().getId())
            .orElseThrow(() -> new NotFoundException("Event of shift plan with id " + shiftPlanId + " not found"));
        var userShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId);

        return DashboardOverviewDto.builder()
            .shiftPlan(ShiftPlanMapper.toShiftPlanDto(shiftPlan))
            .eventOverview(EventMapper.toEventOverviewDto(event))
            .ownShiftPlanStatistics(statisticService.getOwnShiftPlanStatistics(userShifts)) // directly pass user shifts here to avoid redundant filtering
            .overallShiftPlanStatistics(statisticService.getOverallShiftPlanStatistics(shiftPlanId))
            .rewardPoints(-1) // TODO
            .shifts(shiftMapper.assemble(userShifts))
            .trades(null) // TODO implement when trades are available
            .auctions(null) // TODO
            .build();
    }

    @Override
    public ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, String userId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException {
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
            .map(entry -> buildLocationSchedule(entry.getKey(), entry.getValue()))
            .toList();

        var stats = statisticService.getShiftPlanScheduleStatistics(queriedShifts);

        return ShiftPlanScheduleDto.builder()
            .date(searchDto != null ? searchDto.getDate() : null)
            .locations(locationSchedules)
            .scheduleStatistics(stats)
            .build();
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


    private LocationScheduleDto buildLocationSchedule(Location location, List<Shift> shifts) {
        // sort for deterministic column placement
        shifts.sort(Comparator
            .comparing(Shift::getStartTime)
            .thenComparing(Shift::getEndTime));

        var shiftColumns = calculateShiftColumns(shifts);
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

        var firstDate = shifts.stream()
            .map(Shift::getStartTime)
            .min(Instant::compareTo)
            .map(TimeUtil::convertToUtcLocalDate)
            .orElse(null);

        var lastDate = shifts.stream()
            .map(Shift::getEndTime)
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

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) throws NotFoundException {
        return shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));
    }

    @Override
    public ShiftPlanJoinOverviewDto joinShiftPlan(long shiftPlanId, String userId, ShiftPlanJoinRequestDto requestDto) {
        return null;

        // TODO
    }
}
