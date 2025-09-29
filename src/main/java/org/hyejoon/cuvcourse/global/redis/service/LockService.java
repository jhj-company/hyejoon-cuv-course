package org.hyejoon.cuvcourse.global.redis.service;

import java.time.Duration;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.redis.repository.LockRepository;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LockService {

    private final LockRepository lockRepository;

    private static final int LOCK_RETRY_COUNT = 10;
    private static final long LOCK_RETRY_DELAY_MS = 100L;
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final String LOCK_COUNT_PREFIX = ":count";

    public boolean tryAcquire(String lectureKey, long capacity, long currentHeadcount) {
        lockRepository.syncCountFromDb(LOCK_KEY_PREFIX + lectureKey + LOCK_COUNT_PREFIX ,currentHeadcount);

        Long result = lockRepository.executeScript(
            getLectureCounterScript(),
            List.of(LOCK_KEY_PREFIX + lectureKey + LOCK_COUNT_PREFIX),
            String.valueOf(capacity)
        );

        return result == 1;
    }

    public void acquireLockWithRetry(String lectureKey) {
        String lockKey = LOCK_KEY_PREFIX + lectureKey;
        for (int i = 0; i < LOCK_RETRY_COUNT; i++) {
            boolean acquired = lockRepository.acquireLock(lockKey, LOCK_TTL);
            if (acquired) {
                return;
            }
            try {
                Thread.sleep(LOCK_RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(CourseExceptionEnum.INTERNAL_SERVER_ERROR);
            }
        }
        throw new BusinessException(CourseExceptionEnum.CAPACITY_FULL);
    }

    public void releaseLock(String lectureKey) {
        lockRepository.releaseLock(LOCK_KEY_PREFIX + lectureKey);
    }

    public void decrementCount(String lectureKey) {
        lockRepository.decrement(LOCK_KEY_PREFIX + lectureKey);
    }

    private static DefaultRedisScript<Long> getLectureCounterScript() {
        String luaScript =
            "if not redis.call('EXISTS', KEYS[1]) then " +
                "  redis.call('SET', KEYS[1], 0) " +
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
