package org.hyejoon.cuvcourse.global.lock;

public interface DistributedLock {

    boolean acquireLock(String key);

    void releaseLock(String key);

    String getType();
}