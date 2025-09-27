package org.hyejoon.cuvcourse.global.lock;

import java.util.function.Supplier;

public interface DistributedLockManager {

    <T> T executeWithLock(DistributedLockDomain domain, Long key, Supplier<T> task)
        throws InterruptedException;
}
