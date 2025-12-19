package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.LocationScheduleDto;
import at.shiftcontrol.shiftservice.dto.ScheduleStatisticsDto;
import at.shiftcontrol.shiftservice.dto.ShiftColumnDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final StatisticService statisticService;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftDao shiftDao;
    private final EventDao eventDao;

    @Override
    public DashboardOverviewDto getDashboardOverview(long shiftPlanId, long userId) throws NotFoundException {
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var event = eventDao.findById(shiftPlan.getEvent().getId())
            .orElseThrow(() -> new NotFoundException("Event of shift plan with id " + shiftPlanId + " not found"));
        var userShifts = getUserRelatedShifts(shiftPlan, userId);

        return DashboardOverviewDto.builder()
            .shiftPlan(ShiftPlanMapper.toShiftPlanDto(shiftPlan))
            .eventOverview(EventMapper.toEventOverviewDto(event))
            .ownShiftPlanStatistics(statisticService.getOwnShiftPlanStatistics(userShifts)) // directly pass user shifts here to avoid redundant filtering
            .overallShiftPlanStatistics(statisticService.getOverallShiftPlanStatistics(shiftPlanId))
            .rewardPoints(-1) // TODO
            .shifts(ShiftMapper.toShiftDto(userShifts))
            .trades(null) // TODO implement when trades are available
            .auctions(null) // TODO
            .build();
    }

    @Override
    public ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, long userId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException {


        // TODO: ONLY TESTING PURPOSES - REMOVE LATER - DONT COMMIT
        userId = 1;
        var queriedShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId, searchDto);

        // TODO implement logic for ScheduleViewType


        Map<Location, List<Shift>> shiftsByLocation = new HashMap<>();
        for (var shift : queriedShifts) {
            if (shift.getLocations() == null || shift.getLocations().isEmpty()) {
                continue;
            }
            for (var location : shift.getLocations()) {
                shiftsByLocation.computeIfAbsent(location, k -> new ArrayList<>()).add(shift);
            }
        }

        // Build location DTOs
        var locationSchedules = shiftsByLocation.entrySet().stream()
            .map(entry -> buildLocationSchedule(entry.getKey(), entry.getValue()))
            .toList();

        return ShiftPlanScheduleDto.builder()
            .date(searchDto != null ? searchDto.getDate() : null)
            .locations(locationSchedules)
            .build();
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

        var stats = calculateStatistics(shifts);

        return LocationScheduleDto.builder()
            .location(LocationMapper.toLocationDto(location))
            .activities(activities)
            .requiredShiftColumns(requiredShiftColumns)
            .shiftColumns(shiftColumns)
            .scheduleStatistics(stats)
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
                .shiftDto(ShiftMapper.toShiftDto(shift))
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

    private ScheduleStatisticsDto calculateStatistics(List<Shift> shifts) {
        int totalShifts = shifts.size();

        double totalHours = shifts.stream()
            .mapToDouble(s -> TimeUtil.calculateDurationInMinutes(s.getStartTime(), s.getEndTime()))
            .sum() / 60.0;

        long unassignedCount = shifts.stream()
            .flatMap(s -> s.getSlots() == null ? Stream.empty() : s.getSlots().stream())
            .flatMap(slot -> slot.getAssignments() == null ? Stream.empty() : slot.getAssignments().stream())
            .filter(a -> a.getAssignedVolunteer() == null)
            .count();

        return ScheduleStatisticsDto.builder()
            .totalShifts(totalShifts)
            .totalHours(totalHours)
            .unassignedCount((int) unassignedCount)
            .build();
    }


    @Override
    public ShiftPlanJoinOverviewDto joinShiftPlan(long shiftPlanId, long userId, ShiftPlanJoinRequestDto requestDto) {
        return null;

        // TOOD use static mapper
    }

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) throws NotFoundException {
        return shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));
    }

    // TODO maybe do this in the dao layer with a query for performance reasons
    private List<Shift> getUserRelatedShifts(ShiftPlan shiftPlan, long userId) throws NotFoundException {
        return shiftPlan.getShifts().stream()
            .filter(shift -> shift.getSlots().stream()
                .anyMatch(slot -> slot.getAssignments().stream()
                    .anyMatch(assignment -> assignment.getAssignedVolunteer() != null && assignment.getAssignedVolunteer().getId() == userId)))
            .toList();
    }

}
