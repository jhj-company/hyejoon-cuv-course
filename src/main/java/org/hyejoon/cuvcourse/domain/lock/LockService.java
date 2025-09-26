package org.hyejoon.cuvcourse.domain.lock;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockService {

    private static final String LOCK_PREFIX = "LOCK:";
    private static final long LOCK_TIMEOUT_MS = 3000;
    private static final long SLEEP_INTERVAL_MS = 100;

    private final LockRedisRepository lockRedisRepository;
    private final CourseCreateService courseCreateService;

    public Boolean decrease(Long studentId, Long lectureId) throws InterruptedException {
        String key = buildLockKey(studentId, lectureId);
        long startTime = System.currentTimeMillis();

        while (!lockRedisRepository.lock(key)) {
            if (System.currentTimeMillis() - startTime > LOCK_TIMEOUT_MS) {
                throw new RuntimeException("락 획득 실패: " + key);
            }
            Thread.sleep(SLEEP_INTERVAL_MS);
        }

        try {
            courseCreateService.createCourse(studentId, lectureId);
            return true;
        } finally {
            lockRedisRepository.unlock(key);
        }

    }

    private String buildLockKey(Long studentId, Long lectureId) {
        return LOCK_PREFIX + studentId + ":" + lectureId;
    }
}
