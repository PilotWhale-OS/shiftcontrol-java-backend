package at.shiftcontrol.shiftservice.dao;

import java.util.List;

import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterDto;

public interface ShiftDao extends BasicDao<Shift, Long> {
    List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, String userId);

    List<Shift> searchShiftsInShiftPlan(long shiftPlanId, String userId, EventScheduleFilterDto filterDto);
}
