package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;

public interface DashboardService {
    DashboardOverviewDto getDashboardOverviewOfShiftPlan(long shiftPlanId) throws NotFoundException, ForbiddenException;

    Collection<DashboardOverviewDto> getDashboardOverviewsOfAllShiftPlans(String userId) throws NotFoundException, ForbiddenException;
}
