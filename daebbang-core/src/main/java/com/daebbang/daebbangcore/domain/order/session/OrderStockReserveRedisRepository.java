package com.daebbang.daebbangcore.domain.order.session;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderStockReserveRedisRepository {

    private static final String PREFIX = "order:stock-reserve:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String orderNumber, List<OrderSessionItem> items) {
        try {
            String json = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(PREFIX + orderNumber, json, TTL);
            log.info("[StockReserve] 저장 - orderNumber: {}", orderNumber);
        } catch (JacksonException e) {
            log.error("[StockReserve] 직렬화 실패 - orderNumber: {}", orderNumber, e);
            throw new RuntimeException("StockReserve 직렬화 실패", e);
        }
    }

    public Optional<List<OrderSessionItem>> findByOrderNumber(String orderNumber) {
        Object raw = redisTemplate.opsForValue().get(PREFIX + orderNumber);
        if (raw == null) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(raw.toString(), new TypeReference<List<OrderSessionItem>>() {}));
        } catch (JacksonException e) {
            log.error("[StockReserve] 역직렬화 실패 - orderNumber: {}", orderNumber, e);
            throw new RuntimeException("StockReserve 역직렬화 실패", e);
        }
    }

    public void delete(String orderNumber) {
        redisTemplate.delete(PREFIX + orderNumber);
        log.info("[StockReserve] 삭제 - orderNumber: {}", orderNumber);
    }
}
