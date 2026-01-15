package at.shiftcontrol.shiftservice.service.event;

import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleLayoutDto;

public interface EventScheduleService {
    EventScheduleLayoutDto getEventScheduleLayout(long eventId, EventScheduleFilterDto filterDto);

    EventScheduleContentDto getEventScheduleContent(long eventId, EventScheduleDaySearchDto searchDto);

    EventScheduleFilterValuesDto getEventScheduleFilterValues(long eventId);
}
