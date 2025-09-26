package org.hyejoon.cuvcourse.domain.course.courseregist.service;

public interface DistributedLock {

    boolean acquireLock(String key);

    void releaseLock(String key);
}