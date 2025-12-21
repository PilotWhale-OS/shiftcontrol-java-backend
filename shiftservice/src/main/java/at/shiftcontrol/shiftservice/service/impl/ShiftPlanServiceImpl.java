package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
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
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import at.shiftcontrol.shiftservice.service.StatisticService;

@Service
@RequiredArgsConstructor
public class ShiftPlanServiceImpl implements ShiftPlanService {
    private final StatisticService statisticService;
    private final EligibilityService eligibilityService;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftDao shiftDao;
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;

    @Override
    public DashboardOverviewDto getDashboardOverview(long shiftPlanId, String userId) throws NotFoundException {
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
    public ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, String userId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException {
        var queriedShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId, searchDto);

        Map<Location, List<Shift>> shiftsByLocation = new HashMap<>();
        for (var shift : queriedShifts) {
            if (shift.getLocation() == null) {
                continue;
            }
            shiftsByLocation.computeIfAbsent(shift.getLocation(), k -> new ArrayList<>()).add(shift);
        }

        var volunteer = volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with user id: " + userId));

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
    public ShiftPlanJoinOverviewDto joinShiftPlan(long shiftPlanId, String userId, ShiftPlanJoinRequestDto requestDto) {
        return null;

        // TODO
    }
}
