package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;

public interface EventService {
    List<EventDto> search(EventSearchDto searchDto, String userId) throws NotFoundException;

    List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) throws NotFoundException;

    EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException;
}
