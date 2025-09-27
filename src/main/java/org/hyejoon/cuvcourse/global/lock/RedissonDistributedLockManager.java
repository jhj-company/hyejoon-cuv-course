package org.hyejoon.cuvcourse.global.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedissonDistributedLockManager implements DistributedLockManager {

    private static final String LOCK_KEY_PREFIX = "distributed-lock:";
    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(
        DistributedLockDomain domain,
        Long key,
        Supplier<T> task
    ) throws InterruptedException {

        String lockKey = LOCK_KEY_PREFIX + domain.getDomain() + ":" + key;
        /*
         * Fair Lock (공정 락)
         * - 락을 요청한 순서대로 락을 획득할 수 있도록 보장
         * - 내부적으로 pub-sub 방식을 사용해 대기 중인 스레드에게 락 획득 가능 여부를 알림
         */
        RLock lock = redissonClient.getFairLock(lockKey);

        /*
         * tryLock(long waitTime, long leaseTime, TimeUnit unit)
         * - waitTime(10초): 락을 획득하기 위해 최대 10초까지 기다림
         * - leaseTime(15초): 락을 획득한 후 15초가 지나면 자동으로 락 해제 (데드락 방지)
         * - TimeUnit.SECONDS: 메서드에 전달된 시간 값들의 단위를 초 단위로 지정
         *
         * 락 획득에 성공하면 true, 실패하면 false 반환
         */
        if (lock.tryLock(10, 15, TimeUnit.SECONDS)) {
            try {
                return task.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            throw new BusinessException(DistributedLockExceptionEnum.LOCK_ACQUISITION_FAILED);
        }
    }
}
