package org.hyejoon.cuvcourse.global.lock;

import java.util.function.Supplier;
import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;

public interface DistributedLockManager {

    <T> T executeWithLockOrThrow(
        String key,
        long waitTimeMillis,
        long retryIntervalMillis,
        Supplier<T> task,
        BusinessExceptionEnum businessExceptionEnum
    );
}
