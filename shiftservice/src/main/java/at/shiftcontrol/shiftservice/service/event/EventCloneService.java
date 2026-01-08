package at.shiftcontrol.shiftservice.service.event;

import at.shiftcontrol.shiftservice.dto.event.EventDto;

public interface EventCloneService {
    public EventDto cloneEvent(long eventId);
}
