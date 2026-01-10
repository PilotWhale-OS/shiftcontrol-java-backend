package at.shiftcontrol.trustservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import at.shiftcontrol.trustservice.service.AlertService;
import at.shiftcontrol.trustservice.service.RedisService;
import at.shiftcontrol.trustservice.service.TrustService;

@TestConfiguration
public class TrustServiceTestConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
        RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisService redisService(StringRedisTemplate redisTemplate) {
        return new RedisService(redisTemplate);
    }

    @Bean
    public TrustService trustService(
        RedisService redisService,
        AlertService alertService
    ) {
        return new TrustService(redisService, alertService);
    }
}
