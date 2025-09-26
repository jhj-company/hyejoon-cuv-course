package org.hyejoon.cuvcourse.global.lock;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LettuceDistributedLockManager implements DistributedLockManager {

    private final LettuceDistributedLockRepository lockRepository;

    @Override
    public <T> T executeWithLockOrThrow(
        String key,
        long waitTimeMillis,
        long retryIntervalMillis,
        Supplier<T> task,
        BusinessExceptionEnum businessExceptionEnum
    ) {
        if (!tryLock(key, waitTimeMillis, retryIntervalMillis)) {
            throw new BusinessException(businessExceptionEnum);
        }

        try {
            return task.get();
        } finally {
            lockRepository.unlock(key);
        }
    }

    // Spin Lock 방식으로 락 획득 시도
    private boolean tryLock(String key, long waitTimeMillis, long retryIntervalMillis) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < waitTimeMillis) {
            if (lockRepository.lock(key)) {
                return true;
            }

            try {
                // busy-waiting 방지
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException e) {
                // 인터럽트 상태임을 현재 쓰레드에 알림
                Thread.currentThread().interrupt();
                // 인터럽트 발생 시 예외를 던지고 즉시 중단
                throw new IllegalStateException("락 획득 대기 중 인터럽트 발생: " + key, e);
            }
        }

        return false;
    }
}
