package org.hyejoon.cuvcourse.global.redis;

import java.time.Duration;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LockService {

    private final StringRedisTemplate redisTemplate;

    public boolean tryAcquire(String lectureKey, long capacity, long currentHeadcount) {
        String lockKey = "lock:" + lectureKey;
        String countKey = lectureKey + ":count";

        // Lock 획득 시도
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));

        if (!Boolean.TRUE.equals(lockAcquired)) {
            return false;
        }

        try {
            DefaultRedisScript<Long> script = getLectureCounterScript();

            Long result = redisTemplate.execute(
                script,
                List.of(countKey),
                String.valueOf(capacity),
                String.valueOf(currentHeadcount)
            );

            return result == 1;
        } finally {
            // Lock 해제
            redisTemplate.delete(lockKey);
        }
    }

    private static DefaultRedisScript<Long> getLectureCounterScript() {
        String luaScript =
            "if redis.call('GET', KEYS[1]) == false then " +
                "  redis.call('SET', KEYS[1], ARGV[2]) " +
                "end " +
                "local count = redis.call('INCR', KEYS[1]) " +
                "if tonumber(count) > tonumber(ARGV[1]) then " +
                "  redis.call('DECR', KEYS[1]) " +
                "  return 0 " +
                "else " +
                "  return 1 " +
                "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        return script;
    }
}
