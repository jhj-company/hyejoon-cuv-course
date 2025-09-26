package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock;

public interface DistributedLock {

    boolean acquireLock(String key);

    void releaseLock(String key);
}