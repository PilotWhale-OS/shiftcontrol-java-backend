package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
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
        // TODO: do this + filtering in dao layer (date, shiftName, scheduleViewTypes, roleNames, locations, tags) --> Also maybe in other methods where user related shifts are fetched
//        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
//        var userShifts = getUserRelatedShifts(shiftPlan, userId);
//
//        int pageNumber = searchDto.getPageNumber() != null && searchDto.getPageNumber() >= 0
//            ? searchDto.getPageNumber()
//            : 0;
//        int pageSize = searchDto.getPageSize() != null && searchDto.getPageSize() > 0
//            ? searchDto.getPageSize()
//            : 20;
//        int totalElements = userShifts.size();
//
//        List<Shift> pagedShifts = applyPagination(userShifts, pageNumber, pageSize, totalElements);
//
//        double totalHours = userShifts.stream()
//            .mapToDouble(shift -> TimeUtil.calculateDurationInMinutes(shift.getStartTime(), shift.getEndTime()))
//            .sum() / 60.0;
//
//        int unassignedCount = (int) userShifts.stream()
//            .flatMap(shift -> shift.getSlots().stream())
//            .flatMap(slot -> slot.getAssignments().stream())
//            .filter(assignment -> assignment.getAssignedVolunteer() == null)
//            .count();
//
//        return ShiftPlanScheduleDto.builder()
//            .shifts(ShiftMapper.toShiftDto(pagedShifts)) // TODO use filtered and paged shifts here
//            .totalElements(totalElements)
//            .pageNumber(pageNumber)
//            .pageSize(pageSize)
//            .totalShifts(totalElements)
//            .totalHours(totalHours)
//            .unassignedCount(unassignedCount)
//            .build();
        return null;
    }

    private List<Shift> applyPagination(List<Shift> shifts, int pageNumber, int pageSize, int totalElements) {
        int fromIndex = pageNumber * pageSize;
        if (fromIndex > totalElements) {
            fromIndex = totalElements;
        }
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        return shifts.subList(fromIndex, toIndex);
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
