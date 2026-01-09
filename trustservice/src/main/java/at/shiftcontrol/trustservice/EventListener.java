package at.shiftcontrol.trustservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.event.events.EventEvent;

@Slf4j
@Component
public class EventListener {
    private final ObjectMapper objectMapper;

    public EventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${trust.rabbitmq.queue}")
    public void onMessage(
        @Payload EventEvent event,
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) throws JsonProcessingException {
        log.info("Received event [{}] from user [{}] at [{}]",
            routingKey,
            event.getActingUserId(),
            event.getTimestamp()
        );
    }
}
