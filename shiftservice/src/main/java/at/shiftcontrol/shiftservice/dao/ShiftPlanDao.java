package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.lib.entity.ShiftPlan;

public interface ShiftPlanDao extends BasicDao<ShiftPlan, Long> {
    Collection<ShiftPlan> findByEventId(Long eventId);

    Collection<ShiftPlan> findAllUserRelatedShiftPlans(String userId);
}
