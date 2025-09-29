package org.hyejoon.cuvcourse.domain.lock;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockService {

    private static final String LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;

    public void executeWithLock(String key, Runnable task) {
        RLock lock = redissonClient.getLock(key);
        long startTime = System.currentTimeMillis();

        try {
            boolean acquireLock = lock.tryLock(1, 3, TimeUnit.SECONDS);

            if (!acquireLock) {
                throw new RuntimeException("락 획득 실패: " + key);
            }

            task.run();

        } catch (InterruptedException e) {
            throw new RuntimeException("락 대기 중 인터럽트 발생", e);
        } finally {
            lock.unlock();
        }
    }

    public String buildLockKey(Long studentId, Long lectureId) {
        return LOCK_PREFIX + studentId + ":" + lectureId;
    }
}
