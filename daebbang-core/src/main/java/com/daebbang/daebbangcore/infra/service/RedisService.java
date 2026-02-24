package com.daebbang.daebbangcore.infra.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setData(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
        log.info("Redis Data 저장 - key:{}, value:{}, 시간:{}", key, value, duration);
    }

    public String getData(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? String.valueOf(value) : null;
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
        log.info("Redis Data 삭제 - key:{}", key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
