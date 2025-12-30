package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;

public interface ShiftPlanEventService {
    EventShiftPlansOverviewDto getShiftPlansOverview(long eventId) throws NotFoundException;
}
