package at.shiftcontrol.trustservice.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.type.TrustAlertType;
import at.shiftcontrol.trustservice.util.TestEntityFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TrustServiceTest {
    private InMemoryRedisService redisService;
    private AlertService alertService;
    private TrustService trustService;

    @BeforeEach
    void setup() {
        redisService = new InMemoryRedisService();
        alertService = mock(AlertService.class);
        trustService = new TrustService(redisService, alertService);
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
        trustService.handlePositionSlotLeft(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_LEFT, userId, positionSlotId));
        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));

        verify(alertService, never()).sendAlert(any(), eq(userId));

        trustService.handlePositionSlotLeft(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_LEFT, userId, positionSlotId));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.SPAM), eq(userId));

        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, positionSlotId));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.SPAM), eq(userId));
    }

    @Test
    void testOverloadAlert() {
        String userId = "42";
        int i = 0;
        for (; i < 4; i++) {
            trustService.handlePositionSlotJoined(
                TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i));
        }
        verify(alertService, never()).sendAlert(any(), eq(userId));

        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i++));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.OVERLOAD), eq(userId));

        trustService.handlePositionSlotJoined(
            TestEntityFactory.getPositionSlotVolunteerEvent(RoutingKeys.POSITIONSLOT_JOINED, userId, i));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.OVERLOAD), eq(userId));
    }

    @Test
    void testTradeAlert() {
        String userId = "42";
        long offeringPositionSlotId = 1L;
        int i = 0;
        for (; i < 9; i++) {
            trustService.handleTradeRequestCreated(
                TestEntityFactory.getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED, userId, offeringPositionSlotId, String.valueOf(i), i));
        }
        verify(alertService, never()).sendAlert(any(), eq(userId));

        trustService.handleTradeRequestCreated(
            TestEntityFactory.getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED, userId, offeringPositionSlotId, String.valueOf(i), i));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.TRADE), eq(userId));

        trustService.handleTradeRequestCreated(
            TestEntityFactory.getTradeEvent(RoutingKeys.TRADE_REQUEST_CREATED, userId, offeringPositionSlotId, String.valueOf(i), i));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.TRADE), eq(userId));
    }

    @Test
    void testAuctionAlert() {
        String userId = "42";
        int i = 0;
        for (; i < 4; i++) {
            trustService.handleAuctionCreated(
                TestEntityFactory.getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, i));
        }
        verify(alertService, never()).sendAlert(any(), eq(userId));

        trustService.handleAuctionCreated(
            TestEntityFactory.getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, i++));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.AUCTION), eq(userId));

        trustService.handleAuctionCreated(
            TestEntityFactory.getAssignmentEvent(RoutingKeys.AUCTION_CREATED, userId, i));
        verify(alertService, times(1)).sendAlert(eq(TrustAlertType.AUCTION), eq(userId));
    }

    private static final class InMemoryRedisService extends RedisService {
        private final Map<String, Set<String>> overloadSlotsByUser = new HashMap<>();
        private final Map<String, Integer> spamCountsByUserAndSlot = new HashMap<>();
        private final Map<String, Set<String>> tradesByUser = new HashMap<>();
        private final Map<String, Set<String>> auctionsByUser = new HashMap<>();

        private InMemoryRedisService() {
            super(mock(StringRedisTemplate.class));
        }

        @Override
        public void addSignUp(String userId, String slotId, Instant timestamp) {
            overloadSlotsByUser.computeIfAbsent(userId, ignored -> new HashSet<>()).add(slotId);
            spamCountsByUserAndSlot.merge(spamKey(userId, slotId), 1, Integer::sum);
        }

        @Override
        public void removeSignUp(String userId, String slotId, Instant timestamp) {
            overloadSlotsByUser.computeIfAbsent(userId, ignored -> new HashSet<>()).remove(slotId);
            spamCountsByUserAndSlot.merge(spamKey(userId, slotId), 1, Integer::sum);
        }

        @Override
        public void addTrade(String offeringUserId, String offeringSlotId, String requestingUserId, String requestingSlotId) {
            tradesByUser.computeIfAbsent(offeringUserId, ignored -> new HashSet<>())
                .add(offeringUserId + ":" + offeringSlotId + ":" + requestingUserId + ":" + requestingSlotId);
        }

        @Override
        public void removeTrade(String offeringUserId, String offeringSlotId, String requestingUserId, String requestingSlotId) {
            tradesByUser.computeIfAbsent(offeringUserId, ignored -> new HashSet<>())
                .remove(offeringUserId + ":" + offeringSlotId + ":" + requestingUserId + ":" + requestingSlotId);
        }

        @Override
        public void addAuction(String userId, String auctionId) {
            auctionsByUser.computeIfAbsent(userId, ignored -> new HashSet<>()).add(auctionId);
        }

        @Override
        public void removeAuction(String userId, String auctionId) {
            auctionsByUser.computeIfAbsent(userId, ignored -> new HashSet<>()).remove(auctionId);
        }

        @Override
        public boolean hasTooManySignups(String userId, Instant timestamp) {
            return overloadSlotsByUser.getOrDefault(userId, Set.of()).size() >= 5;
        }

        @Override
        public boolean hasTooManySignupsAndOffs(String userId, String slotId, Instant timestamp) {
            return spamCountsByUserAndSlot.getOrDefault(spamKey(userId, slotId), 0) >= 6;
        }

        @Override
        public boolean hasTooManyTrades(String userId) {
            return tradesByUser.getOrDefault(userId, Set.of()).size() >= 10;
        }

        @Override
        public boolean hasTooManyAuctions(String userId) {
            return auctionsByUser.getOrDefault(userId, Set.of()).size() >= 5;
        }

        @Override
        public void resetSpam(String userId, String slotId) {
            spamCountsByUserAndSlot.remove(spamKey(userId, slotId));
        }

        @Override
        public void resetOverload(String userId) {
            overloadSlotsByUser.remove(userId);
        }

        @Override
        public void resetTrades(String userId) {
            tradesByUser.remove(userId);
        }

        @Override
        public void resetAuctions(String userId) {
            auctionsByUser.remove(userId);
        }

        private String spamKey(String userId, String slotId) {
            return userId + ":" + slotId;
        }
    }
}
