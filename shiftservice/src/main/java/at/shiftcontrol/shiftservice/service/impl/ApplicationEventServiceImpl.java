package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.config.RabbitMqConfig;
import at.shiftcontrol.shiftservice.event.ApplicationEvent;
import at.shiftcontrol.shiftservice.event.ApplicationEventWrapper;
import at.shiftcontrol.shiftservice.event.EventClassifier;
import at.shiftcontrol.shiftservice.service.ApplicationEventService;

@Service
@RequiredArgsConstructor
public class ApplicationEventServiceImpl implements ApplicationEventService {
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationUserProvider userProvider;
    private final Tracer tracer;

    @Override
    public void publishEvent(ApplicationEvent event, String routingKey) {
        var wrappedEvent = wrapEvent(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, wrappedEvent);
    }

    private ApplicationEventWrapper wrapEvent(ApplicationEvent event) {
        //Get EventType from EventClassifier annotation and wrap event
        var classifier = event.getClass().getAnnotation(EventClassifier.class);
        if (classifier == null) {
            throw new IllegalArgumentException("Event class " + event.getClass().getName() + " is not annotated with @EventClassifier");
        }
        var eventType = classifier.value();
        return ApplicationEventWrapper.builder()
            .timestamp(Instant.now())
            .actingUserId(userProvider.getCurrentUser().getUserId())
            .traceId(getTraceId())
            .eventType(eventType)
            .event(event)
            .build();
    }

    private String getTraceId() {
        var context = this.tracer.currentTraceContext().context();
        return context != null ? context.traceId() : null;
    }
}
