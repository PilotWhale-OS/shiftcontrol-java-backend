package at.shiftcontrol.trustservice.service;

import org.springframework.stereotype.Service;

import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;

@Service
public class TrustService {

    private final RedisService redisService;

    public TrustService(RedisService redisService) {
        this.redisService = redisService;
    }

    public void handlePositionSlotJoined(PositionSlotVolunteerEvent event) {
        redisService.set(event.getVolunteerId(), event.getRoutingKey());
        String test = redisService.get(event.getVolunteerId());
        System.out.println("REDIS test: " + test);
    }

    public void handlePositionSlotLeft(PositionSlotVolunteerEvent event) {

    }

    public void handleTradeRequestCreated(TradeEvent event) {

    }

    public void handleTradeRequestDeclined(TradeEvent event) {

    }

    public void handleTradeRequestCanceled(TradeEvent event) {

    }

    public void handleTradeRequestCompleted(AssignmentSwitchEvent event) {

    }

    public void handleAuctionCreated(AssignmentEvent event) {

    }

    public void handleAuctionClaimed(AssignmentEvent event) {

    }

    public void handleAuctionCanceled(AssignmentEvent event) {

    }

    public void handlePositionSlotRequestLeave(PositionSlotVolunteerEvent event) {

    }

    public void handlePositionSlotRequestLeaveAccepted(PositionSlotVolunteerEvent event) {

    }

    public void handlePositionSlotRequestLeaveDeclined(PositionSlotVolunteerEvent event) {

    }
}
