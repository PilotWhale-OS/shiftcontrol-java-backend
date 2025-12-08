package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.EventOverviewDto;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;

public interface EventService {
    List<EventOverviewDto> search(EventSearchDto searchDto);

    EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, long userId) throws NotFoundException;
}
