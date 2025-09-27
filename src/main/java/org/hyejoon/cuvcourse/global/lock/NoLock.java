package org.hyejoon.cuvcourse.global.lock;

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

    @Override
    public String getType() {
        return "NoLock";
    }
}