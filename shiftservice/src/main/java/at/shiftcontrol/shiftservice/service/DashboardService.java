package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.event.EventsDashboardOverviewDto;

public interface DashboardService {
    ShiftPlanDashboardOverviewDto getDashboardOverviewOfShiftPlan(long shiftPlanId) throws NotFoundException, ForbiddenException;

    EventsDashboardOverviewDto getDashboardOverviewsOfAllShiftPlans(String userId) throws NotFoundException, ForbiddenException;
}
