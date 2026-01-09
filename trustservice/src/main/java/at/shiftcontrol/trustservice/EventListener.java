package at.shiftcontrol.trustservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventListener {

    @RabbitListener(queues = "${trust.rabbitmq.queue}")
    public void onMessage(
        @Payload EventEvent event,
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) {
        log.info("Received event [{}] from user [{}] at [{}]",
            routingKey,
            event.getActingUserId(),
            event.getTimestamp()
        );

        // Later:
        // trustEvaluationService.process(event, routingKey);
    }
}
