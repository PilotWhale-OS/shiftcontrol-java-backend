package at.shiftcontrol.shiftservice.service.impl.event;

import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.service.event.EventCloneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCloneServiceImpl implements EventCloneService {

    @Override
    @AdminOnly
    public EventDto cloneEvent(long eventId) {
        return null;
    }
}
