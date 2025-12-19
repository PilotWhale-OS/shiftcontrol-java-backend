package at.shiftcontrol.shiftservice.dao;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Shift;

public interface ShiftDao extends BasicDao<Shift, Long> {
    List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, long userId);

    List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, long userId, ShiftPlanScheduleSearchDto searchDto);
}
