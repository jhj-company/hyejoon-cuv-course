package org.hyejoon.cuvcourse.domain.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockService {

    private static final String LOCK_PREFIX = "LOCK:";
    private static final long LOCK_TIMEOUT_MS = 3000;
    private static final long SLEEP_INTERVAL_MS = 100;

    private final LockRedisRepository lockRedisRepository;

    public void executeWithLock(String key, Runnable task) {
        long startTime = System.currentTimeMillis();

        try {
            while (!lockRedisRepository.lock(key)) {
                if (System.currentTimeMillis() - startTime > LOCK_TIMEOUT_MS) {
                    throw new RuntimeException("락 획득 실패: " + key);
                }
                Thread.sleep(SLEEP_INTERVAL_MS);
            }

            task.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 대기 중 인터럽트 발생", e);
        } finally {
            lockRedisRepository.unlock(key);
        }
    }

    public String buildLockKey(Long studentId, Long lectureId) {
        return LOCK_PREFIX + studentId + ":" + lectureId;
    }
}
