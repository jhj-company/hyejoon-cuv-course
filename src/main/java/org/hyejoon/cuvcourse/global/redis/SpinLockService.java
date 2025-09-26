package org.hyejoon.cuvcourse.global.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpinLockService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String LOCK_VALUE = "1";

    public Boolean acquireLock(String lockKey, long timeoutSeconds) {
        return stringRedisTemplate.opsForValue().setIfAbsent(lockKey, LOCK_VALUE, Duration
            .ofSeconds(timeoutSeconds));
    }

    public void releaseLock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }
}
