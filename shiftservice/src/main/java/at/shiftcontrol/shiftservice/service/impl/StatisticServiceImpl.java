package at.shiftcontrol.shiftservice.service.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.ScheduleStatisticsDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final ShiftPlanDao shiftPlanDao;
    private final EventDao eventDao;

    @Override
    public OwnStatisticsDto getOwnShiftPlanStatistics(List<Shift> userShifts) {
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
    public OverallStatisticsDto getOverallShiftPlanStatistics(long shiftPlanId) {
        var shiftPlan = shiftPlanDao.findById(shiftPlanId).get(); // no validation needed since this is done in calling service method
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
    public OwnStatisticsDto getOwnEventStatistics(long eventId, long userId) {
        var event = eventDao.findById(eventId).get(); // no validation needed since this is done in calling service method
        var userShiftPlans = event.getShiftPlans().stream()
            .filter(shiftPlan -> shiftPlan.getShifts().stream()
                .anyMatch(shift -> shift.getSlots().stream()
                    .anyMatch(slot -> slot.getAssignments().stream()
                        .anyMatch(assignment -> assignment.getAssignedVolunteer() != null && assignment.getAssignedVolunteer().getId() == userId))))
            .toList();

        var totalHours = userShiftPlans.stream()
            .mapToDouble(shiftPlan ->
                calculateTotalHoursInShifts(shiftPlan.getShifts().stream().toList()))
            .sum();

        var totalShifts = userShiftPlans.stream()
            .mapToInt(shiftPlan ->
                calculateTotalShiftCountInShifts(shiftPlan.getShifts().stream().toList()))
            .sum();
        var busyDays = userShiftPlans.stream()
            .mapToInt(shiftPlan ->
                calculateBusyDaysInShifts(shiftPlan.getShifts().stream().toList()))
            .sum();

        return OwnStatisticsDto.builder()
            .totalHours(totalHours)
            .totalShifts(totalShifts)
            .busyDays(busyDays)
            .build();
    }

    @Override
    public OverallStatisticsDto getOverallEventStatistics(long eventId) {
        var event = eventDao.findById(eventId).get(); // no validation needed since this is done in calling service method

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
        var volunteerSet = new HashSet<Long>();
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
