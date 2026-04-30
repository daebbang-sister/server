package com.daebbang.daebbangcore.domain.order.session;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderSessionRedisRepository {

    private static final String PREFIX = "order:session:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String orderNumber, OrderSession session) {
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(PREFIX + orderNumber, json, TTL);
            log.info("[OrderSession] 저장 - orderNumber: {}", orderNumber);
        } catch (JacksonException e) {
            log.error("[OrderSession] 직렬화 실패 - orderNumber: {}", orderNumber, e);
            throw new RuntimeException("OrderSession 직렬화에 실패했습니다.", e);
        }
    }

    public Optional<OrderSession> findByOrderNumber(String orderNumber) {
        Object raw = redisTemplate.opsForValue().get(PREFIX + orderNumber);
        if (raw == null) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(raw.toString(), OrderSession.class));
        } catch (JacksonException e) {
            log.error("[OrderSession] 역직렬화 실패 - orderNumber: {}", orderNumber, e);
            throw new RuntimeException("OrderSession 역직렬화에 실패했습니다.", e);
        }
    }

    public void delete(String orderNumber) {
        redisTemplate.delete(PREFIX + orderNumber);
        log.info("[OrderSession] 삭제 - orderNumber: {}", orderNumber);
    }
}
