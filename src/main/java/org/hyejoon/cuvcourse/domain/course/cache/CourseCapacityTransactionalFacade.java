package org.hyejoon.cuvcourse.domain.course.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
public class CourseCapacityTransactionalFacade {

    /**
     * 트랜잭션 롤백 또는 트랜잭션 미사용 상황에서 지정된 동작을 실행한다.
     */
    public void executeOnRollback(long lectureId, Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(lectureId, "rollback-immediate", action);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    runSafely(lectureId, "rollback", action);
                }
            }
        });
    }

    /**
     * 트랜잭션 커밋 이후 지정된 동작을 실행한다.
     */
    public void executeAfterCommit(long lectureId, Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(lectureId, "commit-immediate", action);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runSafely(lectureId, "commit", action);
            }
        });
    }

    private void runSafely(long lectureId, String phase, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            log.error("강의 정원 캐시 후처리 중 예외가 발생했습니다. lectureId={}, phase={}", lectureId, phase, ex);
        }
    }
}
