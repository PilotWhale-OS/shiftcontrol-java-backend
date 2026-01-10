package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;
import java.util.Set;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;

public interface ShiftPlanDao extends BasicDao<ShiftPlan, Long> {
    Collection<ShiftPlan> findByEventId(Long eventId);

    Collection<ShiftPlan> findAllUserRelatedShiftPlans(String userId);

    Collection<ShiftPlan> getByIds(Set<Long> shiftPlanIds);
}
