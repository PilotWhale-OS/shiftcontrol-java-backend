package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.event.ApplicationEvent;

public interface ApplicationEventService {
    void publishEvent(ApplicationEvent event, String routingKey);
}
