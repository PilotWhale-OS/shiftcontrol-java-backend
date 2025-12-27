package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.event.EventsDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDashboardOverviewDto;

public interface DashboardService {
    ShiftPlanDashboardOverviewDto getDashboardOverviewOfShiftPlan(long shiftPlanId) throws NotFoundException, ForbiddenException;

    EventsDashboardOverviewDto getDashboardOverviewsOfAllShiftPlans(String userId) throws NotFoundException, ForbiddenException;
}
