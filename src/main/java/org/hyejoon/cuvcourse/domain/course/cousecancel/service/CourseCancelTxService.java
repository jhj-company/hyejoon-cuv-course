package org.hyejoon.cuvcourse.domain.course.cousecancel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCacheException;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityTransactionalFacade;
import org.hyejoon.cuvcourse.domain.course.cousecancel.exception.CourseCancelExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCancelTxService {

    private final CourseJpaRepository courseJpaRepository;
    private final CourseCapacityCache courseCapacityCache;
    private final CourseCapacityTransactionalFacade courseCapacityTransactionalFacade;

    @Transactional
    void cancelWithTransaction(Long lectureId, Long studentId) {
        Course course = courseJpaRepository.findByLectureAndStudent(lectureId, studentId)
            .orElseThrow(() -> new BusinessException(CourseCancelExceptionEnum.COURSE_NOT_FOUND));

        courseCapacityCache.getOrInit(lectureId);

        courseJpaRepository.delete(course);

        courseCapacityTransactionalFacade.executeAfterCommit(
            lectureId,
            () -> releaseWithRecovery(lectureId)
        );
    }

    private void releaseWithRecovery(long lectureId) {
        try {
            courseCapacityCache.release(lectureId);
            return;
        } catch (CourseCapacityCacheException ex) {
            log.warn(
                "Redis 수강 신청 취소 처리에 실패해 재시드를 시도합니다. lectureId={}",
                lectureId,
                ex
            );
        }

        try {
            courseCapacityCache.getOrInit(lectureId);
            courseCapacityCache.release(lectureId);
        } catch (CourseCapacityCacheException retryEx) {
            log.error(
                "Redis 수강 신청 취소 재시도 중 예외가 발생했습니다. lectureId={}",
                lectureId,
                retryEx
            );
        }
    }

}
