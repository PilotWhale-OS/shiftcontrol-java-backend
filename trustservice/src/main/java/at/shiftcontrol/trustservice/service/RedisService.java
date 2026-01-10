package at.shiftcontrol.trustservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final StringRedisTemplate redis;

    public RedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void set(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redis.opsForValue().get(key);
    }

}
