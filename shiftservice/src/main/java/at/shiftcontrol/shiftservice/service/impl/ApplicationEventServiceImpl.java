package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.config.RabbitMqConfig;
import at.shiftcontrol.shiftservice.event.ApplicationEvent;
import at.shiftcontrol.shiftservice.event.ApplicationEventWrapper;
import at.shiftcontrol.shiftservice.service.ApplicationEventService;

@Service
@RequiredArgsConstructor
public class ApplicationEventServiceImpl implements ApplicationEventService {
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationUserProvider userProvider;

    @Override
    public void publishEvent(ApplicationEvent event, String routingKey) {
        ApplicationEventWrapper.builder()
            .timestamp(Instant.now())
            .actingUserId(userProvider.getCurrentUser().getUserId())
            .event(event);

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, event);
    }
}
