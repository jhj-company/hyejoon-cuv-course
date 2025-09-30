package org.hyejoon.cuvcourse.domain.course.courseregist.listener;

import org.hyejoon.cuvcourse.domain.course.courseregist.event.CourseRegistCompensationEvent;
import org.hyejoon.cuvcourse.domain.lecture.cache.LectureCacheService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseRegistCompensationListener {

    private final LectureCacheService lectureCacheService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void courseRegistFailed(CourseRegistCompensationEvent event) {
        lectureCacheService.decreaseLectureTotal(event.lecture());
    }
}
