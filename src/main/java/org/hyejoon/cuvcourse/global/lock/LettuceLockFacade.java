package org.hyejoon.cuvcourse.global.lock;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LettuceLockFacade {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean acquireLock(String key, long timeoutSeconds) {
        return redisTemplate
            .opsForValue()
            .setIfAbsent(key, "lock", Duration.ofSeconds(timeoutSeconds));
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }

}
