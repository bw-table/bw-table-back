package com.zero.bwtableback.common;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisConnectionChecker {

    private StringRedisTemplate redisTemplate;

    @Autowired
    public RedisConnectionChecker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void checkRedisConnection() {
        try {
            // Redis에 PING 명령어를 보내 연결 확인
            String response = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equals(response)) {
                log.info("Redis에 성공적으로 연결되었습니다.");
            }
        } catch (Exception e) {
            log.error("Redis 연결 실패: {}", e.getMessage());
        }
    }
}
