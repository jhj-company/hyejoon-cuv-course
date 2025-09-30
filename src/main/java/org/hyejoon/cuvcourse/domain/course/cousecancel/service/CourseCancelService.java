package org.hyejoon.cuvcourse.domain.course.cousecancel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.enums.CourseLockPrefixEnum;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCancelService {

    private final LockManager lockManager;
    private final DistributedLock distributedLock;
    private final CourseCancelTxService courseCancelTxService;

    public void courseCancel(Long lectureId, Long studentId) {
        String lockKey = CourseLockPrefixEnum.COURSE_LOCK.getPrefix() + lectureId;

        lockManager.executeWithLock(distributedLock, lockKey, () -> {
            courseCancelTxService.cancelWithTransaction(lectureId, studentId);
            return null;
        });
    }
}
