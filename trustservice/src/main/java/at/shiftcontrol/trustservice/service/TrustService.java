package at.shiftcontrol.trustservice.service;

import org.springframework.stereotype.Service;

import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;
import at.shiftcontrol.lib.type.TrustAlertType;

@Service
public class TrustService {

    private final RedisService redisService;
    private final AlertService alertService;

    public TrustService(RedisService redisService, AlertService alertService) {
        this.redisService = redisService;
        this.alertService = alertService;
    }

    public void handlePositionSlotJoined(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        redisService.addSignUp(userId, slotId);
        checkSpam(userId, slotId);
        checkOverload(userId, slotId);
    }

    public void handlePositionSlotLeft(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        redisService.removeSignUp(userId, slotId);
        checkSpam(userId, slotId);
    }

    public void handleTradeRequestCreated(TradeEvent event) {
        String offeringUserId = event.getTrade().getOfferingAssignment().getVolunteerId();
        String offeringSlotId = String.valueOf(event.getTrade().getOfferingAssignment().getPositionSlot().getPositionSlotId());
        String requestingUserId = event.getTrade().getRequestedAssignment().getVolunteerId();
        String requestingSlotId = String.valueOf(event.getTrade().getRequestedAssignment().getPositionSlot().getPositionSlotId());
        redisService.addTrade(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
        checkTrade(offeringUserId, offeringSlotId);
    }

    public void handleTradeRequestDeclined(TradeEvent event) {
        String offeringUserId = event.getTrade().getOfferingAssignment().getVolunteerId();
        String offeringSlotId = String.valueOf(event.getTrade().getOfferingAssignment().getPositionSlot().getPositionSlotId());
        String requestingUserId = event.getTrade().getRequestedAssignment().getVolunteerId();
        String requestingSlotId = String.valueOf(event.getTrade().getRequestedAssignment().getPositionSlot().getPositionSlotId());
        redisService.removeTrade(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
    }

    public void handleTradeRequestCanceled(TradeEvent event) {
        String offeringUserId = event.getTrade().getOfferingAssignment().getVolunteerId();
        String offeringSlotId = String.valueOf(event.getTrade().getOfferingAssignment().getPositionSlot().getPositionSlotId());
        String requestingUserId = event.getTrade().getRequestedAssignment().getVolunteerId();
        String requestingSlotId = String.valueOf(event.getTrade().getRequestedAssignment().getPositionSlot().getPositionSlotId());
        redisService.removeTrade(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
    }

    public void handleTradeRequestCompleted(AssignmentSwitchEvent event) {
        String offeringUserId = event.getOfferingAssignment().getVolunteerId();
        String offeringSlotId = String.valueOf(event.getOfferingAssignment().getPositionSlot().getPositionSlotId());
        String requestingUserId = event.getRequestedAssignment().getVolunteerId();
        String requestingSlotId = String.valueOf(event.getRequestedAssignment().getPositionSlot().getPositionSlotId());
        redisService.removeTrade(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
    }

    public void handleAuctionCreated(AssignmentEvent event) {
        String userId = event.getAssignment().getVolunteerId();
        String slotId = String.valueOf(event.getAssignment().getPositionSlot().getPositionSlotId());
        redisService.addAuction(userId, slotId);
        checkAuction(userId, slotId);
    }

    public void handleAuctionClaimed(AssignmentEvent event) {
        int lastDot = event.getRoutingKey().lastIndexOf('.');
        String oldUserId = event.getRoutingKey().substring(lastDot + 1);
        String slotId = String.valueOf(event.getAssignment().getPositionSlot().getPositionSlotId());
        redisService.removeAuction(oldUserId, slotId);
    }

    public void handleAuctionCanceled(AssignmentEvent event) {
        String userId = event.getAssignment().getVolunteerId();
        String slotId = String.valueOf(event.getAssignment().getPositionSlot().getPositionSlotId());
        redisService.removeAuction(userId, slotId);
    }

    public void handlePositionSlotRequestLeave(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        redisService.addAuction(userId, slotId);
        checkAuction(userId, slotId);
    }

    public void handlePositionSlotRequestLeaveAccepted(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        redisService.removeAuction(userId, slotId);
    }

    public void handlePositionSlotRequestLeaveDeclined(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        redisService.removeAuction(userId, slotId);
    }

    // ============================================
    // CHECKS FOR ALERTS
    // ============================================

    private void checkSpam(String userId, String slot) {
        if (redisService.hasTooManySignupsAndOffs(userId, slot)) {
            alertService.sendAlert(TrustAlertType.SPAM, userId, slot);
            redisService.resetSpam(userId, slot);
        }
    }

    private void checkOverload(String userId, String slot) {
        if (redisService.hasTooManySignups(userId)) {
            alertService.sendAlert(TrustAlertType.OVERLOAD, userId, slot);
            redisService.resetOverload(userId);
        }
    }

    private void checkTrade(String userId, String slot) {
        if (redisService.hasTooManyTrades(userId)) {
            alertService.sendAlert(TrustAlertType.TRADE, userId, slot);
            redisService.resetTrades(userId);
        }
    }

    private void checkAuction(String userId, String slot) {
        if (redisService.hasTooManyAuctions(userId)) {
            alertService.sendAlert(TrustAlertType.AUCTION, userId, slot);
            redisService.resetAuctions(userId);
        }
    }
}
