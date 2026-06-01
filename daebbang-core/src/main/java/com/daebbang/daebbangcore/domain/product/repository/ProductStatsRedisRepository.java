package com.daebbang.daebbangcore.domain.product.repository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductStatsRedisRepository {

    private static final String KEY_PREFIX = "product:stats:";

    private final RedisTemplate<String, Object> redisTemplate;

    private String key(Long productId) {
        return KEY_PREFIX + productId;
    }

    public void increment(Long productId, String field, long delta) {
        redisTemplate.opsForHash().increment(key(productId), field, delta);
    }

    public Map<String, Long> getAndClear(Long productId) {
        String lua = """
            local data = redis.call('HGETALL', KEYS[1])
            redis.call('DEL', KEYS[1])
            return data
            """;

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(List.class);

        List<Object> result = redisTemplate.execute(script, List.of(key(productId)));

        Map<String, Long> map = new HashMap<>();
        if (result == null || result.isEmpty()) {
            return map;
        }

        for (int i = 0; i + 1 < result.size(); i += 2) {
            String field = String.valueOf(result.get(i));
            long value = Long.parseLong(String.valueOf(result.get(i + 1)));
            map.put(field, value);
        }
        return map;
    }

    public Set<Long> getTrackedProductIds() {
        Set<Long> ids = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions()
            .match(KEY_PREFIX + "*")
            .count(100)
            .build();

        List<String> keys = new ArrayList<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                cursor.forEachRemaining(key ->
                    keys.add(new String(key, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                log.error("[ProductStats] SCAN 실패", e);
            }
            return null;
        });

        for (String key : keys) {
            try {
                String idStr = key.substring(KEY_PREFIX.length());
                ids.add(Long.parseLong(idStr));
            } catch (NumberFormatException e) {
                log.warn("[ProductStats] 잘못된 키 형식: {}", key);
            }
        }
        return ids;
    }
}
