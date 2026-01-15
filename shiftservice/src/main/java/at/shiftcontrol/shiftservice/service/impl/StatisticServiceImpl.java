package at.shiftcontrol.shiftservice.service.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.event.schedule.ScheduleStatisticsDto;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final ShiftDao shiftDao;

    @Override
    public OwnStatisticsDto getOwnStatisticsOfShifts(List<Shift> userShifts) {
        var busyDays = calculateBusyDaysInShifts(userShifts);
        var totalShifts = calculateTotalShiftCountInShifts(userShifts);
        var totalHours = calculateTotalHoursInShifts(userShifts);

        return OwnStatisticsDto.builder()
            .busyDays(busyDays)
            .totalShifts(totalShifts)
            .totalHours(totalHours)
            .build();
    }


    @Override
    public OverallStatisticsDto getOverallShiftPlanStatistics(ShiftPlan shiftPlan) {
        var totalShifts = shiftPlan.getShifts().size();
        var totalHours = calculateTotalHoursInShifts(shiftPlan.getShifts().stream().toList());
        var volunteerCount = calculateVolunteerCountInShiftPlan(shiftPlan);

        return OverallStatisticsDto.builder()
            .totalHours(totalHours)
            .totalShifts(totalShifts)
            .volunteerCount(volunteerCount)
            .build();
    }

    @Override
    public OverallStatisticsDto getOverallEventStatistics(Event event) {
        var totalHours = event.getShiftPlans().stream()
            .mapToDouble(shiftPlan ->
                calculateTotalHoursInShifts(shiftPlan.getShifts().stream().toList()))
            .sum();
        var totalShifts = event.getShiftPlans().stream()
            .mapToInt(shiftPlan ->
                calculateTotalShiftCountInShifts(shiftPlan.getShifts().stream().toList()))
            .sum();
        var volunteerCount = event.getShiftPlans().stream()
            .mapToInt(this::calculateVolunteerCountInShiftPlan)
            .sum();

        return OverallStatisticsDto.builder()
            .totalHours(totalHours)
            .totalShifts(totalShifts)
            .volunteerCount(volunteerCount)
            .build();
    }

    @Override
    public OwnStatisticsDto getOwnStatisticsOfShiftPlans(List<ShiftPlan> shiftPlans, String userId) {
        var userShiftsSet = new HashSet<Shift>();
        for (var shiftPlan : shiftPlans) {
            var relevantShiftsOfPlan = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlan.getId(), userId);
            userShiftsSet.addAll(relevantShiftsOfPlan);
        }

        return getOwnStatisticsOfShifts(userShiftsSet.stream().toList());
    }

    @Override
    public ScheduleStatisticsDto getShiftPlanScheduleStatistics(List<Shift> shifts) {
        double totalHours = shifts.stream()
            .mapToDouble(s -> TimeUtil.calculateDurationInMinutes(s.getStartTime(), s.getEndTime()))
            .sum() / 60.0;

        long unassignedCount = shifts.stream()
            .flatMap(s -> s.getSlots() == null ? Stream.empty() : s.getSlots().stream())
            .flatMap(slot -> slot.getAssignments() == null ? Stream.empty() : slot.getAssignments().stream())
            .filter(a -> a.getAssignedVolunteer() == null)
            .count();

        return ScheduleStatisticsDto.builder()
            .totalShifts(shifts.size())
            .totalHours(totalHours)
            .unassignedCount((int) unassignedCount)
            .build();
    }

    private int calculateBusyDaysInShifts(List<Shift> userShifts) {
        var busyDaysSet = new HashSet<LocalDate>();
        for (var shift : userShifts) {
            var shiftDate = TimeUtil.convertToUtcLocalDate(shift.getStartTime());
            busyDaysSet.add(shiftDate);
        }
        return busyDaysSet.size();
    }

    private int calculateTotalShiftCountInShifts(List<Shift> userShifts) {
        return userShifts.size();
    }

    private double calculateTotalHoursInShifts(List<Shift> userShifts) {
        double totalMinutes = 0;
        for (var shift : userShifts) {
            var duration = TimeUtil.calculateDurationInMinutes(shift.getStartTime(), shift.getEndTime());
            totalMinutes += duration;
        }
        return totalMinutes / 60;
    }

    private int calculateVolunteerCountInShiftPlan(ShiftPlan shiftPlan) {
        var volunteerSet = new HashSet<String>();
        for (var shift : shiftPlan.getShifts()) {
            for (var slot : shift.getSlots()) {
                for (var assignment : slot.getAssignments()) {
                    if (assignment.getAssignedVolunteer() != null) {
                        volunteerSet.add(assignment.getAssignedVolunteer().getId());
                    }
                }
            }
        }
        return volunteerSet.size();
    }
}
