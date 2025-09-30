package org.hyejoon.cuvcourse.global.lock;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.exception.GlobalExceptionEnum;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockManager {

    private static final int LOCK_ACQUIRE_RETRY_DELAY_MS = 1000;
    private static final int LOCK_ACQUIRE_MAX_RETRY = 3;

    public <T> T executeWithLock(DistributedLock distributedLock, String key,
        Callable<T> callable) {
        boolean acquired = false;
        int attempts = 0;

        while (!acquired && attempts < LOCK_ACQUIRE_MAX_RETRY) {
            acquired = distributedLock.acquireLock(key);
            if (!acquired) {
                try {
                    Thread.sleep(LOCK_ACQUIRE_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(GlobalExceptionEnum.LOCK_ACQUIRE_FAILED);
                }
            }
            attempts++;
        }

        if (!acquired) {
            throw new BusinessException(GlobalExceptionEnum.LOCK_ACQUIRE_FAILED);
        }

        // 트랜잭션이 활성화된 경우
        // 트랜잭션 종료 시점과 락 해제 시점을 맞춰주기 위해 Callback을 등록
        boolean syncActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (syncActive) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        distributedLock.releaseLock(key);
                    }
                });
        }

        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Exception occured: {}", e.getMessage());
            throw new RuntimeException("Lock Acquire Failed", e);
        } finally {
            // 트랜잭션이 비활성화된 경우 즉시 락 해제
            if (!syncActive) {
                distributedLock.releaseLock(key);
            }
        }
    }

    public void executeWithLock(DistributedLock distributedLock, String key,
        Runnable runnable) throws Exception {
        this.executeWithLock(distributedLock, key, () -> {
            runnable.run();
            return null;
        });
    }
}
