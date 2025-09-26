package org.hyejoon.cuvcourse.global.lock;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LettuceDistributedLockRepository {

    private static final String LOCK_VALUE = "lock";
    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(String key) {
        return redisTemplate
            .opsForValue()
            .setIfAbsent(
                generateKey(key),
                LOCK_VALUE,
                Duration.ofMillis(3_000) // 데드락 방지 (3초 후 자동 해제)
            );
    }

    public void unlock(String key) {
        redisTemplate.delete(generateKey(key));
    }

    private String generateKey(String key) {
        return key;
    }
}
