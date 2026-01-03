package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;

public interface EventService {
    EventDto getEvent(long eventId) throws NotFoundException, ForbiddenException;

    List<EventDto> search(EventSearchDto searchDto) throws NotFoundException;

    List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) throws NotFoundException;

    EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException;

    EventScheduleDto getEventSchedule(long eventId, EventScheduleDaySearchDto searchDto) throws NotFoundException, ForbiddenException;

    EventDto createEvent(EventModificationDto modificationDto);

    EventDto updateEvent(long eventId, EventModificationDto eventModificationDto) throws NotFoundException;

    void deleteEvent(long eventId) throws NotFoundException;
}
