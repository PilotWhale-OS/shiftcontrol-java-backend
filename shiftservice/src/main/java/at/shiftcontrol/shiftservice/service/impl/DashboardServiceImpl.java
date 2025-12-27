package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.EventsDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDashboardOverviewDto;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.DashboardService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final StatisticService statisticService;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftDao shiftDao;
    private final EventDao eventDao;
    private final ApplicationUserProvider userProvider;
    private final ShiftAssemblingMapper shiftMapper;

    @Override
    public ShiftPlanDashboardOverviewDto getDashboardOverviewOfShiftPlan(long shiftPlanId) throws NotFoundException, ForbiddenException {
        var userId = validateShiftPlanAccessAndGetUserId(shiftPlanId);
        var shiftPlan = getShiftPlanOrThrow(shiftPlanId);
        var event = eventDao.findById(shiftPlan.getEvent().getId())
            .orElseThrow(() -> new NotFoundException("Event of shift plan with id " + shiftPlanId + " not found"));
        var userShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId);

        return ShiftPlanDashboardOverviewDto.builder()
            .shiftPlan(ShiftPlanMapper.toShiftPlanDto(shiftPlan))
            .eventOverview(EventMapper.toEventOverviewDto(event))
            .ownShiftPlanStatistics(statisticService.getOwnStatisticsOfShifts(userShifts)) // directly pass user shifts here to avoid redundant filtering
            .overallShiftPlanStatistics(statisticService.getOverallShiftPlanStatistics(shiftPlan))
            .rewardPoints(-1) // TODO
            .shifts(shiftMapper.assemble(userShifts))
            .trades(null) // TODO implement when trades are available
            .auctions(null) // TODO
            .build();
    }

    private String validateShiftPlanAccessAndGetUserId(long shiftPlanId) throws ForbiddenException {
        var currentUser = userProvider.getCurrentUser();
        if (!(currentUser.isVolunteerInPlan(shiftPlanId) || currentUser.isPlannerInPlan(shiftPlanId))) {
            throw new ForbiddenException("User has no access to shift plan with id: " + shiftPlanId);
        }

        return currentUser.getUserId();
    }

    private ShiftPlan getShiftPlanOrThrow(long shiftPlanId) throws NotFoundException {
        return shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found with id: " + shiftPlanId));
    }

    @Override
    public EventsDashboardOverviewDto getDashboardOverviewsOfAllShiftPlans(String userId) throws NotFoundException, ForbiddenException {
        var userShiftPlans = shiftPlanDao.findAllUserRelatedShiftPlans(userId);

        var shiftPlanDashboards = userShiftPlans.stream().map(shiftPlan -> {
            try {
                return getDashboardOverviewOfShiftPlan(shiftPlan.getId());
            } catch (NotFoundException | ForbiddenException e) {
                // This should not happen as we already fetched user related shift plans
                throw new RuntimeException(e);
            }
        }).toList();

        return EventsDashboardOverviewDto.builder()
            .shiftPlanDashboardOverviewDtos(shiftPlanDashboards)
            .ownStatisticsDto(statisticService.getOwnStatisticsOfShiftPlans(userShiftPlans.stream().toList(), userId))
            .build();
    }
}
