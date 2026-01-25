package at.shiftcontrol.trustservice.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.ClaimedAuctionEvent;
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
        Instant timestamp = event.getTimestamp();
        redisService.addSignUp(userId, slotId, timestamp);
        checkSpam(userId, slotId, timestamp);
        checkOverload(userId, timestamp);
    }

    public void handlePositionSlotLeft(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        Instant timestamp = event.getTimestamp();
        redisService.removeSignUp(userId, slotId, timestamp);
        checkSpam(userId, slotId, timestamp);
    }

    public void handleTradeRequestCreated(TradeEvent event) {
        String offeringUserId = event.getTrade().getOfferingAssignment().getVolunteerId();
        String offeringSlotId = String.valueOf(event.getTrade().getOfferingAssignment().getPositionSlot().getPositionSlotId());
        String requestingUserId = event.getTrade().getRequestedAssignment().getVolunteerId();
        String requestingSlotId = String.valueOf(event.getTrade().getRequestedAssignment().getPositionSlot().getPositionSlotId());
        redisService.addTrade(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
        checkTrade(offeringUserId);
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
        checkAuction(userId);
    }

    public void handleAuctionClaimed(ClaimedAuctionEvent event) {
        String oldUserId = event.getOldVolunteerId();
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
        Instant timestamp = event.getTimestamp();
        redisService.addSignUp(userId, slotId, timestamp);
        checkSpam(userId, slotId, timestamp);
        redisService.addAuction(userId, slotId);
        checkAuction(userId);
    }

    public void handlePositionSlotRequestLeaveWithdraw(PositionSlotVolunteerEvent event) {
        String userId = event.getVolunteerId();
        String slotId = String.valueOf(event.getPositionSlot().getPositionSlotId());
        Instant timestamp = event.getTimestamp();
        redisService.removeAuction(userId, slotId);
        redisService.removeSignUp(userId, slotId, timestamp);
        checkSpam(userId, slotId, timestamp);
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

    private void checkSpam(String userId, String slot, Instant timestamp) {
        if (redisService.hasTooManySignupsAndOffs(userId, slot, timestamp)) {
            alertService.sendAlert(TrustAlertType.SPAM, userId);
            redisService.resetSpam(userId, slot);
        }
    }

    private void checkOverload(String userId, Instant timestamp) {
        if (redisService.hasTooManySignups(userId, timestamp)) {
            alertService.sendAlert(TrustAlertType.OVERLOAD, userId);
            redisService.resetOverload(userId);
        }
    }

    private void checkTrade(String userId) {
        if (redisService.hasTooManyTrades(userId)) {
            alertService.sendAlert(TrustAlertType.TRADE, userId);
            redisService.resetTrades(userId);
        }
    }

    private void checkAuction(String userId) {
        if (redisService.hasTooManyAuctions(userId)) {
            alertService.sendAlert(TrustAlertType.AUCTION, userId);
            redisService.resetAuctions(userId);
        }
    }
}
