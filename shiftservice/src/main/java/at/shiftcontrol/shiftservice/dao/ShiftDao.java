package at.shiftcontrol.shiftservice.dao;

import java.util.List;

import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.shiftservice.dto.event.schedule.EventScheduleFilterDto;

public interface ShiftDao extends BasicDao<Shift, Long> {
    List<Shift> searchUserRelatedShiftsInEvent(long eventId, String userId);

    List<Shift> searchShiftsInEvent(long eventId, String userId, EventScheduleFilterDto filterDto);
}
