package at.shiftcontrol.trustservice.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;
import at.shiftcontrol.trustservice.service.TrustService;

@Slf4j
@Component
public class EventListener {
    private final ObjectMapper objectMapper;
    private final TrustService trustService;

    private static final String ROUTING_KEY_PREFIX = "shiftcontrol.";

    public EventListener(
        ObjectMapper objectMapper,
        TrustService trustService
    ) {
        this.objectMapper = objectMapper;
        this.trustService = trustService;
    }

    @RabbitListener(queues = "${trust.rabbitmq.queue}")
    public void onMessage(
        @Payload String rawJson,
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) throws JsonProcessingException {
        try {
            log.info("Received message: routingKey={}, payload={}",
                routingKey, rawJson);

            if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_JOINED_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotJoined(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_LEFT_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotLeft(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.TRADE_REQUEST_CREATED_PREFIX)) {
                TradeEvent event =
                    objectMapper.readValue(rawJson, TradeEvent.class);
                trustService.handleTradeRequestCreated(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.TRADE_REQUEST_DECLINED_PREFIX)) {
                TradeEvent event =
                    objectMapper.readValue(rawJson, TradeEvent.class);
                trustService.handleTradeRequestDeclined(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.TRADE_REQUEST_CANCELED_PREFIX)) {
                TradeEvent event =
                    objectMapper.readValue(rawJson, TradeEvent.class);
                trustService.handleTradeRequestCanceled(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.TRADE_REQUEST_COMPLETED_PREFIX)) {
                AssignmentSwitchEvent event =
                    objectMapper.readValue(rawJson, AssignmentSwitchEvent.class);
                trustService.handleTradeRequestCompleted(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.AUCTION_CREATED_PREFIX)) {
                AssignmentEvent event =
                    objectMapper.readValue(rawJson, AssignmentEvent.class);
                trustService.handleAuctionCreated(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.AUCTION_CLAIMED_PREFIX)) {
                AssignmentEvent event =
                    objectMapper.readValue(rawJson, AssignmentEvent.class);
                trustService.handleAuctionClaimed(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.AUCTION_CANCELED_PREFIX)) {
                AssignmentEvent event =
                    objectMapper.readValue(rawJson, AssignmentEvent.class);
                trustService.handleAuctionCanceled(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotRequestLeaveAccepted(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotRequestLeaveDeclined(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_REQUEST_JOIN_WITHDRAW_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotLeft(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_WITHDRAW_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotRequestLeaveWithdraw(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_CREATED_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotRequestLeave(event);

            } else if (routingKey.startsWith(ROUTING_KEY_PREFIX + RoutingKeys.POSITIONSLOT_REQUEST_JOIN_CREATED_PREFIX)) {
                PositionSlotVolunteerEvent event =
                    objectMapper.readValue(rawJson, PositionSlotVolunteerEvent.class);
                trustService.handlePositionSlotJoined(event);

            } else {
                log.info("Not processing routing key: {}", routingKey);
            }
        } catch (JsonProcessingException e) {
            log.error(
                "Failed to process message. routingKey={}, payload={}",
                routingKey,
                rawJson,
                e
            );
        }
    }
}
