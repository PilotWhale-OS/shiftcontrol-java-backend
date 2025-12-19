package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;

public interface EventService {
    List<EventDto> search(EventSearchDto searchDto);

    EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException;
}
