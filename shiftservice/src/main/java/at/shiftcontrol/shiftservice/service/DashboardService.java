package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.event.EventsDashboardOverviewDto;

public interface DashboardService {
    EventsDashboardOverviewDto getDashboardOverviewsOfAllShiftPlans(String userId);
}
