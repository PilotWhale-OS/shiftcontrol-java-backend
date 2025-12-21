package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;

public interface EventService {
    List<EventDto> search(EventSearchDto searchDto);

    List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, long userId) throws NotFoundException;

    EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, long userId) throws NotFoundException;
}
