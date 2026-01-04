package at.shiftcontrol.shiftservice.event;

import java.time.Instant;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.config.RabbitMqConfig;

@Service
@RequiredArgsConstructor
public class RabbitEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationUserProvider userProvider;
    private final Tracer tracer;

    private static final String ROUTING_KEY_PREFIX = "shiftcontrol.";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BaseEvent event) {
        var wrappedEvent = wrapEvent(event);
        var routingKey = event.getRoutingKey();
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException("Event class " + event.getClass().getName() + " returned null or blank routing key");
        }

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, ROUTING_KEY_PREFIX + routingKey, wrappedEvent);
    }

    private ApplicationEventWrapper wrapEvent(BaseEvent event) {
        //Get EventType from EventClassifier annotation and wrap event
//         var classifier = event.getClass().getAnnotation(EventClassifier.class);
//         if (classifier == null) {
//             throw new IllegalArgumentException("Event class " + event.getClass().getName() + " is not annotated with @EventClassifier");
//         }
//         var eventType = classifier.value();
        return ApplicationEventWrapper.builder()
            .timestamp(Instant.now())
            .actingUserId(userProvider.getCurrentUser().getUserId())
            .traceId(getTraceId())
        // .eventType(eventType)
            .event(event)
            .build();
    }

    private String getTraceId() {
        var context = this.tracer.currentTraceContext().context();
        return context != null ? context.traceId() : null;
    }
}
