package at.shiftcontrol.trustservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.trustservice.config.EmbeddedRedisConfig;
import at.shiftcontrol.trustservice.config.TrustServiceTestConfig;
import at.shiftcontrol.trustservice.util.TestEntityFactory;
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
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));
        trustService.handlePositionSlotLeft(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_LEFT, userId, positionSlotId));
        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));

        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handlePositionSlotLeft(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_LEFT, userId, positionSlotId));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("SPAM"), eq(userId));

        // check that counter has been reset
        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));
        verify(alertService, Mockito.times(1)).sendAlert(eq("SPAM"), eq(userId));
    }

    @Test
    void testOverloadAlert() {
        String userId = "42";
        int i = 0;
        for (; i < 4; i++) {
            trustService.handlePositionSlotJoined(
                TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i));
        }
        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i++));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("OVERLOAD"), eq(userId));

        // check that counter has been reset
        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i++));
        verify(alertService, Mockito.times(1)).sendAlert(eq("OVERLOAD"), eq(userId));
    }

    @Test
    void testTradeAlert() {
        String userId = "42";
        long offeringPositionSlotId = 1l;
        int i = 0;
        for (; i < 9; i++) {
            trustService.handleTradeRequestCreated(
                TestEntityFactory.getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED,
                    userId, offeringPositionSlotId, String.valueOf(i), i));
        }
        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handleTradeRequestCreated(
            TestEntityFactory.getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED,
                userId, offeringPositionSlotId, String.valueOf(i), i++));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("TRADE"), eq(userId));

        // check that counter has been reset
        trustService.handleTradeRequestCreated(
            TestEntityFactory.getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED,
                userId, offeringPositionSlotId, String.valueOf(i), i++));
        verify(alertService, Mockito.times(1)).sendAlert(eq("TRADE"), eq(userId));
    }

    @Test
    void testAuctionAlert() {
        String userId = "42";
        for (int i = 0; i < 2; i++) {
            trustService.handleAuctionCreated(
                TestEntityFactory.getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, i));
        }
        verify(alertService, Mockito.never()).sendAlert(any(), eq(userId));

        // send alert only after threshold
        trustService.handleAuctionCreated(
            TestEntityFactory.getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, 3));
        verify(alertService, Mockito.atLeastOnce()).sendAlert(eq("AUCTION"), eq(userId));

        // check that counter has been reset
        trustService.handleAuctionCreated(
            TestEntityFactory.getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, 4));
        verify(alertService, Mockito.times(1)).sendAlert(eq("AUCTION"), eq(userId));
    }
}
