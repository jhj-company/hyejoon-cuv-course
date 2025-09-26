package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import java.util.concurrent.TimeUnit;

import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedissonLock implements DistributedLock {

    private static final int LOCK_LEASE_SECONDS = 3;
    private static final int LOCK_MAX_WAIT_SECONDS = 3;

    private final RedissonClient redissonClient;

    @Override
    public boolean acquireLock(String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(LOCK_MAX_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CourseRegistExceptionEnum.LOCK_ACQUIRE_FAILED);
        }
    }

    @Override
    public void releaseLock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}