package at.shiftcontrol.trustservice.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisService {
    @Getter
    private final StringRedisTemplate redis;

    private static final long SPAM_WINDOW_SECONDS = 3600;       // 1h
    private static final long SPAM_THRESHOLD = 6;
    private static final long OVERLOAD_WINDOW_SECONDS = 1800;   // 30m
    private static final long OVERLOAD_THRESHOLD = 5;
    private static final long TRADE_THRESHOLD = 10;
    private static final long AUCTION_THRESHOLD = 5;

    public RedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ============================================
    // SLOT SIGNUP / LEAVE
    // ============================================

    public void addSignUp(String userId, String slotId, Instant timestamp) {
        long now = timestamp.toEpochMilli();

        // OVERLOAD tracking: add to signups ZSET
        redis.opsForZSet().add(overloadKey(userId), slotId, now);

        // SPAM tracking: record JOIN
        redis.opsForZSet().add(spamKey(userId, slotId), "JOIN:" + now, now);

        cleanupOld(overloadKey(userId), OVERLOAD_WINDOW_SECONDS, timestamp);
        cleanupOld(spamKey(userId, slotId), SPAM_WINDOW_SECONDS, timestamp);

        // set TTLs
        redis.expire(overloadKey(userId), Duration.ofHours(1));
        redis.expire(spamKey(userId, slotId), Duration.ofHours(2));
    }

    public void removeSignUp(String userId, String slotId, Instant timestamp) {
        long now = timestamp.toEpochMilli();

        // OVERLOAD tracking: remove from signups ZSET
        redis.opsForZSet().remove(overloadKey(userId), slotId);

        // SPAM tracking: record LEAVE
        redis.opsForZSet().add(spamKey(userId, slotId), "LEAVE:" + now, now);

        cleanupOld(spamKey(userId, slotId), SPAM_WINDOW_SECONDS, timestamp);

        redis.expire(spamKey(userId, slotId), Duration.ofHours(2));
    }

    // ============================================
    // TRADE: Open trade requests
    // ============================================

    public void addTrade(String offeringUserId, String offeringSlotId,
                         String requestingUserId, String requestingSlotId) {
        String tradeId = makeTradeId(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
        redis.opsForSet().add(tradeKey(offeringUserId), tradeId);
    }

    public void removeTrade(String offeringUserId, String offeringSlotId,
                         String requestingUserId, String requestingSlotId) {
        String tradeId = makeTradeId(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId);
        redis.opsForSet().remove(tradeKey(offeringUserId), tradeId);
    }

    // ============================================
    // AUCTION: Open auctions
    // ============================================~

    public void addAuction(String userId, String auctionId) {
        redis.opsForSet().add(auctionKey(userId), auctionId);
    }

    public void removeAuction(String userId, String auctionId) {
        redis.opsForSet().remove(auctionKey(userId), auctionId);
    }

    // ============================================
    // QUERIES
    // ============================================

    public boolean hasTooManySignups(String userId, Instant timestamp) {
        cleanupOld(overloadKey(userId), OVERLOAD_WINDOW_SECONDS, timestamp);
        log.info("CHECK OVERLOAD: {}", redis.opsForZSet().zCard(overloadKey(userId)));
        return redis.opsForZSet().zCard(overloadKey(userId)) >= OVERLOAD_THRESHOLD;
    }

    public boolean hasTooManySignupsAndOffs(String userId, String slotId, Instant timestamp) {
        cleanupOld(spamKey(userId, slotId), SPAM_WINDOW_SECONDS, timestamp);
        log.info("CHECK SPAM: {}", redis.opsForZSet().zCard(spamKey(userId, slotId)));
        return redis.opsForZSet().zCard(spamKey(userId, slotId)) >= SPAM_THRESHOLD;
    }

    public boolean hasTooManyTrades(String userId) {
        log.info("CHECK TRADE: {}", redis.opsForSet().size(tradeKey(userId)));
        return redis.opsForSet().size(tradeKey(userId)) >= TRADE_THRESHOLD;
    }

    public boolean hasTooManyAuctions(String userId) {
        log.info("CHECK AUCTION: {}", redis.opsForSet().size(auctionKey(userId)));
        return redis.opsForSet().size(auctionKey(userId)) >= AUCTION_THRESHOLD;
    }

    // ============================================
    // HELPERS
    // ============================================

    private void cleanupOld(String key, long windowSeconds, Instant timestamp) {
        long cutoff = timestamp.minusSeconds(windowSeconds).getEpochSecond();
        redis.opsForZSet().removeRangeByScore(key, 0, cutoff);
    }

    // RESETS

    public void resetSpam(String userId, String slotId) {
        redis.delete(spamKey(userId, slotId));
    }

    public void resetOverload(String userId) {
        redis.delete(overloadKey(userId));
    }

    public void resetTrades(String userId) {
        redis.delete(tradeKey(userId));
    }

    public void resetAuctions(String userId) {
        redis.delete(auctionKey(userId));
    }

    // KEYS

    private String spamKey(String userId, String slotId) {
        return "trust:" + userId + ":join-leave:" + slotId;
    }

    private String overloadKey(String userId) {
        return "trust:" + userId + ":signups";
    }

    private String tradeKey(String userId) {
        return "trust:" + userId + ":trades";
    }

    private String auctionKey(String userId) {
        return "trust:" + userId + ":auctions";
    }

    private String makeTradeId(String offeringUserId,
                               String offeringSlotId,
                               String requestedUserId,
                               String requestedSlotId) {
        return String.join(":", offeringUserId, offeringSlotId, requestedUserId, requestedSlotId);
    }
}
