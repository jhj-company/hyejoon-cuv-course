package org.hyejoon.cuvcourse.domain.course.cousecancel.listener;

import org.hyejoon.cuvcourse.domain.course.cousecancel.event.CourseCancelCompensationEvent;
import org.hyejoon.cuvcourse.domain.lecture.cache.LectureCacheService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseCancelCompensationListener {

    private final LectureCacheService lectureCacheService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void courseRegistFailed(CourseCancelCompensationEvent event) {
        lectureCacheService.rollbackCacheFrom(event.lectureId());
    }
}
