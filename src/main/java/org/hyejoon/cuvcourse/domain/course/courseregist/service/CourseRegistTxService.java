package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.CourseCapacitySnapshot;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.TryReserveResult;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCacheException;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityTransactionalFacade;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRegistTxService {

    private final CourseJpaRepository courseJpaRepository;
    private final CourseCapacityCache courseCapacityCache;
    private final CourseCapacityTransactionalFacade courseCapacityTransactionalFacade;

    @Transactional
    public Course createCourseIfAvailable(Lecture lecture, CourseId courseId) {
        long lectureId = lecture.getId();

        // 캐시에서 현재 정원 및 신청 인원을 조회해 빠르게 수강신청 가능 여부를 확인한다.
        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(lectureId);
        if (snapshot.headcount() >= snapshot.capacity()) {
            throw new BusinessException(CourseRegistExceptionEnum.CAPACITY_FULL);
        }

        // Redis 수강 신청 시도
        TryReserveResult reserveResult = courseCapacityCache.tryReserve(lectureId);

        // 캐시 초기화가 필요한 경우
        if (reserveResult == TryReserveResult.RETRY_LATER) {
            // 한 번 더 초기화 후
            snapshot = courseCapacityCache.getOrInit(lectureId);

            // 정원 초과 여부를 다시 확인
            if (snapshot.headcount() >= snapshot.capacity()) {
                throw new BusinessException(CourseRegistExceptionEnum.CAPACITY_FULL);
            }

            // 수강 신청 재시도
            reserveResult = courseCapacityCache.tryReserve(lectureId);
        }

        if (reserveResult == TryReserveResult.CAPACITY_FULL) {
            throw new BusinessException(CourseRegistExceptionEnum.CAPACITY_FULL);
        }

        if (reserveResult != TryReserveResult.RESERVED) {
            throw new CourseCapacityCacheException(
                "수강 신청 예약 처리 결과가 비정상입니다. lecture=" + lectureId
            );
        }

        courseCapacityTransactionalFacade.executeOnRollback(
            lectureId,
            () -> releaseOnRollback(lectureId)
        );

        Course course = Course.from(courseId);
        return courseJpaRepository.save(course);
    }

    private void releaseOnRollback(long lectureId) {
        try {
            courseCapacityCache.release(lectureId);
        } catch (CourseCapacityCacheException ex) {
            log.error("트랜잭션 롤백 후 Redis 롤백에 실패했습니다. lectureId={}", lectureId, ex);
        }
    }
}
