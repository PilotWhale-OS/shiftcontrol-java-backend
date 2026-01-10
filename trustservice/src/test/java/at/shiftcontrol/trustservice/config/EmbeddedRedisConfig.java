package at.shiftcontrol.trustservice.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = RedisServer.builder()
            .port(redisPort)
            .setting("maxmemory 128M")
            .build();
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
