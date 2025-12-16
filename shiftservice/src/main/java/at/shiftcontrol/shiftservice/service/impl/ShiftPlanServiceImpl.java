package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
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
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var userShifts = getUserRelatedShifts(shiftPlan, userId);

        // TODO apply search criteria from searchDto

        return ShiftPlanScheduleDto.builder()
            .shifts(ShiftMapper.toShiftDto(userShifts)) // TODO implement pagination
            .totalElements(userShifts.size())
            .pageNumber(searchDto.getPageNumber()) // TODO implement pagination
            .pageSize(searchDto.getPageSize()) // TODO implement pagination
            .totalShifts(userShifts.size())
            .totalHours(userShifts.stream().mapToDouble(shift -> TimeUtil.calculateDurationInMinutes(shift.getStartTime(), shift.getEndTime())).sum() / 60.0)
            .unassignedCount((int) userShifts.stream().flatMap(shift -> shift.getSlots().stream())
                .flatMap(slot -> slot.getAssignments().stream())
                .filter(assignment -> assignment.getAssignedVolunteer() == null)
                .count())
            .build();

    }

    @Override
    public ShiftPlanDto joinShiftPlan(long shiftPlanId, long userId, ShiftPlanJoinRequestDto requestDto) {
        return null;

        // TOOD use static mapper
    }

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) throws NotFoundException {
        return shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));
    }

    private List<Shift> getUserRelatedShifts(ShiftPlan shiftPlan, long userId) throws NotFoundException {
        return shiftPlan.getShifts().stream()
            .filter(shift -> shift.getSlots().stream()
                .anyMatch(slot -> slot.getAssignments().stream()
                    .anyMatch(assignment -> assignment.getAssignedVolunteer() != null && assignment.getAssignedVolunteer().getId() == userId)))
            .toList();
    }

}
