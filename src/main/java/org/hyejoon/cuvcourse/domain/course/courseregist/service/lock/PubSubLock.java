package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PubSubLock implements DistributedLock {

    private static final int LOCK_MAX_WAIT_SECONDS = 3;

    private final RedisLockRegistry redisLockRegistry;

    @Override
    public boolean acquireLock(String key) {
        Lock lock = redisLockRegistry.obtain(key);
        try {
            return lock.tryLock(LOCK_MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CourseRegistExceptionEnum.LOCK_ACQUIRE_FAILED);
        }
    }

    @Override
    public void releaseLock(String key) {
        Lock lock = redisLockRegistry.obtain(key);
        lock.unlock();
    }

    @Override
    public String getType() {
        return "PubSubLock";
    }
}