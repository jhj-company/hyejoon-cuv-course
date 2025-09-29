package org.hyejoon.cuvcourse.global.redis.repository;

import java.time.Duration;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LockRepository {

    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String lockKey, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", ttl);
        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    public Long executeScript(DefaultRedisScript<Long> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }

    public void decrement(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    public void syncCountFromDb(String lectureKey, long currentHeadcount) {
        redisTemplate.opsForValue().setIfAbsent(lectureKey, String.valueOf(currentHeadcount));
    }
}
