package org.hyejoon.cuvcourse.global.redis;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.global.config.RedisConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpinLockService {

    @Qualifier(RedisConfig.LOCK_STRING_TEMPLATE_BEAN_NAME)
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
