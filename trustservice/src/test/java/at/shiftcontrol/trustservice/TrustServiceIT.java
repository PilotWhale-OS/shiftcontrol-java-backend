package at.shiftcontrol.trustservice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;
import at.shiftcontrol.trustservice.config.EmbeddedRedisConfig;
import at.shiftcontrol.trustservice.config.TrustServiceTestConfig;
import at.shiftcontrol.trustservice.service.AlertService;
import at.shiftcontrol.trustservice.service.RedisService;
import at.shiftcontrol.trustservice.service.TrustService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    EmbeddedRedisConfig.class,
    TrustServiceTestConfig.class
})
public class TrustServiceIT {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TrustService trustService;

    @MockitoBean
    private AlertService alertService;

    @BeforeEach
    void setup() {
        redisService.getRedis().getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void testSpamAlert() {
        String userId = "42";
        long positionSlotId = 1L;

        trustService.handlePositionSlotJoined(
            getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));
        trustService.handlePositionSlotLeft(
            getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_LEFT, userId, positionSlotId));
        trustService.handlePositionSlotJoined(
            getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));

        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handlePositionSlotLeft(
            getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_LEFT, userId, positionSlotId));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("SPAM"), eq(userId));
    }

    @Test
    void testOverloadAlert() {
        String userId = "42";
        for (int i = 0; i < 4; i++) {
            trustService.handlePositionSlotJoined(
                getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i));
        }
        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handlePositionSlotJoined(
            getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, 5));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("OVERLOAD"), eq(userId));
    }

    @Test
    void testTradeAlert() {
        String userId = "42";
        long offeringPositionSlotId = 1l;
        for (int i = 0; i < 9; i++) {
            trustService.handleTradeRequestCreated(
                getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED,
                    userId, offeringPositionSlotId, String.valueOf(i), i));
        }
        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handleTradeRequestCreated(
            getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED,
                userId, offeringPositionSlotId, "10", 10));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("TRADE"), eq(userId));
    }

    @Test
    void testAuctionAlert() {
        String userId = "42";
        for (int i = 0; i < 2; i++) {
            trustService.handleAuctionCreated(
                getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, i));
        }
        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handleAuctionCreated(
            getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, 3));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("AUCTION"), eq(userId));
    }

    private PositionSlotVolunteerEvent getPositionSlotVolunteerEvent(String routingKey, String userId, long slotId) {
        PositionSlot positionSlot = getPositionSlot(slotId);
        return PositionSlotVolunteerEvent.of(RoutingKeys.format(routingKey,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", userId)),
            positionSlot, userId);
    }
    private AssignmentEvent getAssignmentEvent(String routingKey, String userId, long slotId) {
        Assignment assignment = getAssignment(userId, slotId);
        return AssignmentEvent.of(routingKey, assignment);
    }

    private TradeEvent getTradeEvent(String routingKey, String offeringUserId, long offeringSlotId,
                                     String requestingUserId, long requestingSlotId) {
        return TradeEvent.of(routingKey, getAssignmentSwitchRequest(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId));
    }

    private AssignmentSwitchRequest getAssignmentSwitchRequest(String offeringUserId, long offeringSlotId,
                                                               String requestingUserId, long requestingSlotId) {
        Assignment offering = getAssignment(offeringUserId, offeringSlotId);
        Assignment requesting = getAssignment(requestingUserId, requestingSlotId);
        return AssignmentSwitchRequest.builder()
            .id(AssignmentSwitchRequestId.of(offering, requesting))
            .offeringAssignment(offering)
            .requestedAssignment(requesting)
            .build();
    }

    private Assignment getAssignment(String volunteerId, long slotId) {
        return Assignment.builder()
            .id(AssignmentId.of(slotId, volunteerId))
            .assignedVolunteer(getVolunteer(volunteerId))
            .positionSlot(getPositionSlot(slotId))
            .build();
    }

    private Volunteer getVolunteer(String id) {
        return Volunteer.builder()
            .id(id)
            .build();
    }

    private PositionSlot getPositionSlot(long id) {
        return PositionSlot.builder()
            .id(id)
            .build();
    }
}
