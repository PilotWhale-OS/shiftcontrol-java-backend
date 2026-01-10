package at.shiftcontrol.trustservice.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
public class RedisService {

    @Getter
    private final StringRedisTemplate redis;

    private static final long SPAM_WINDOW_SECONDS = 3600;       // 1h
    private static final long SPAM_THRESHOLD = 4;
    private static final long OVERLOAD_WINDOW_SECONDS = 1800;   // 30m
    private static final long OVERLOAD_THRESHOLD = 5;
    private static final long TRADE_THRESHOLD = 10;
    private static final long AUCTION_THRESHOLD = 3;

    public RedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ============================================
    // SLOT SIGNUP / LEAVE
    // ============================================

    public void addSignUp(String userId, String slotId) {
        long now = Instant.now().toEpochMilli();

        // OVERLOAD tracking: add to signups ZSET
        redis.opsForZSet().add(overloadKey(userId), slotId, now);

        // SPAM tracking: record JOIN
        redis.opsForZSet().add(spamKey(userId, slotId), "JOIN:" + now, now);

        cleanupOld(overloadKey(userId), OVERLOAD_WINDOW_SECONDS);
        cleanupOld(spamKey(userId, slotId), SPAM_WINDOW_SECONDS);

        // set TTLs
        redis.expire(overloadKey(userId), Duration.ofHours(1));
        redis.expire(spamKey(userId, slotId), Duration.ofHours(2));
    }

    public void removeSignUp(String userId, String slotId) {
        long now = Instant.now().toEpochMilli();

        // OVERLOAD tracking: remove from signups ZSET
        redis.opsForZSet().remove(overloadKey(userId), slotId);

        // SPAM tracking: record LEAVE
        redis.opsForZSet().add(spamKey(userId, slotId), "LEAVE:" + now, now);

        cleanupOld(spamKey(userId, slotId), SPAM_WINDOW_SECONDS);

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

    public boolean hasTooManySignups(String userId) {
        cleanupOld(overloadKey(userId), OVERLOAD_WINDOW_SECONDS);
        return redis.opsForZSet().zCard(overloadKey(userId)) >= OVERLOAD_THRESHOLD;
    }

    public boolean hasTooManySignupsAndOffs(String userId, String slotId) {
        cleanupOld(spamKey(userId, slotId), SPAM_WINDOW_SECONDS);
        System.out.println("CHECK SPAM: " + redis.opsForZSet().zCard(spamKey(userId, slotId)));
        return redis.opsForZSet().zCard(spamKey(userId, slotId)) >= SPAM_THRESHOLD;
    }

    public boolean hasTooManyTrades(String userId) {
        return redis.opsForSet().size(tradeKey(userId)) >= TRADE_THRESHOLD;
    }

    public boolean hasTooManyAuctions(String userId) {
        return redis.opsForSet().size(auctionKey(userId)) >= AUCTION_THRESHOLD;
    }

    // ============================================
    // HELPERS
    // ============================================

    private void cleanupOld(String key, long windowSeconds) {
        long cutoff = Instant.now().minusSeconds(windowSeconds).getEpochSecond();
        redis.opsForZSet().removeRangeByScore(key, 0, cutoff);
    }

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
