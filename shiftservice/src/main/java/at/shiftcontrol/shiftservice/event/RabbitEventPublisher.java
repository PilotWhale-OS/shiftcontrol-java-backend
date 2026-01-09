package at.shiftcontrol.shiftservice.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.config.RabbitMqConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationUserProvider userProvider;
    private final Tracer tracer;

    private static final String ROUTING_KEY_PREFIX = "shiftcontrol.";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactional(BaseEvent event) {
        publishEvent(event);
    }

    @EventListener
    public void onImmediate(BaseEvent event) {
        // If a transaction is active, defer to the transactional listener to avoid duplicate handling.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            log.trace("Skipping immediate handling for event inside active transaction: {}", event.getClass().getName());
            return;
        }
        publishEvent(event);
    }

    private void publishEvent(BaseEvent event) {
        event.setTraceId(getTraceId());
        event.setActingUserId(getCurrentUserId());
        var routingKey = event.getRoutingKey();
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException("Event class " + event.getClass().getName() + " returned null or blank routing key");
        }

        log.trace("Publishing event to RabbitMQ with routing key {}: {}", ROUTING_KEY_PREFIX + routingKey, event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, ROUTING_KEY_PREFIX + routingKey, event);
    }

    private String getTraceId() {
        var context = this.tracer.currentTraceContext().context();
        return context != null ? context.traceId() : null;
    }

    @Nullable
    private String getCurrentUserId() {
        var userOpt = userProvider.getNullableApplicationUser();
        if (userOpt.isPresent()) {
            var user = (ShiftControlUser) userOpt.get();
            return user.getUserId();
        } else {
            return null;
        }
    }
}
