package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.event.EventsDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDashboardOverviewDto;

public interface DashboardService {
    ShiftPlanDashboardOverviewDto getDashboardOverviewOfShiftPlan(long shiftPlanId);

    EventsDashboardOverviewDto getDashboardOverviewsOfAllShiftPlans(String userId);
}
