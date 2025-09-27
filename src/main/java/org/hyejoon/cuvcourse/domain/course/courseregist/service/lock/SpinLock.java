package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock;

import org.hyejoon.cuvcourse.global.redis.SpinLockService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpinLock implements DistributedLock {

    private static final long LOCK_TIMEOUT_SECONDS = 10L;

    private final SpinLockService spinLockService;

    @Override
    public boolean acquireLock(String key) {
        Boolean lockAcquired = spinLockService.acquireLock(key, LOCK_TIMEOUT_SECONDS);
        return lockAcquired != null && lockAcquired;
    }

    @Override
    public void releaseLock(String key) {
        spinLockService.releaseLock(key);
    }

    @Override
    public String getType() {
        return "SpinLock";
    }
}