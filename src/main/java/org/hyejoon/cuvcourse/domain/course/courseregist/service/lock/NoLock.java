package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock;

import org.springframework.stereotype.Component;

@Component
public class NoLock implements DistributedLock {

    @Override
    public boolean acquireLock(String key) {
        return true; // 항상 성공
    }

    @Override
    public void releaseLock(String key) {
        // 아무것도 하지 않음
    }
}