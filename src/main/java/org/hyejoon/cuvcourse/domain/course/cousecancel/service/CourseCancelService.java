package org.hyejoon.cuvcourse.domain.course.cousecancel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.CourseCapacitySnapshot;
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
public class CourseCancelService {

    private final CourseJpaRepository courseJpaRepository;
    private final CourseCapacityCache courseCapacityCache;
    private final CourseCapacityTransactionalFacade courseCapacityTransactionalFacade;

    @Transactional
    public void courseCancel(Long lectureId, Long studentId) {
        Course course = courseJpaRepository.findByLectureAndStudent(lectureId, studentId)
            .orElseThrow(() -> new BusinessException(CourseCancelExceptionEnum.COURSE_NOT_FOUND));

        // 취소 직전의 수강 신청 정보를 저장해 캐시 해제 실패 시 목표 인원을 계산하는데 사용
        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(lectureId);

        courseJpaRepository.delete(course);

        // 트랜잭션 커밋 후 Redis 캐시에서 수강 신청자 수를 감소시킨다.
        courseCapacityTransactionalFacade.executeAfterCommit(
            lectureId,
            () -> releaseWithRecovery(lectureId, snapshot.headcount())
        );
    }

    private void releaseWithRecovery(long lectureId, long headcountBeforeCancel) {
        long targetHeadcount = Math.max(headcountBeforeCancel - 1, 0);

        try {
            // 수강 신청자 수 감소 시도
            courseCapacityCache.release(lectureId);
            return;
        } catch (CourseCapacityCacheException ex) {
            log.warn("Redis 수강 신청 취소 처리에 실패해 재초기화를 시도합니다. lectureId={}, targetHeadcount={}",
                lectureId, targetHeadcount, ex);
        }

        // 캐시 초기화 후 재시도
        CourseCapacitySnapshot snapshotAfter = courseCapacityCache.getOrInit(lectureId);
        long currentHeadcount = snapshotAfter.headcount();

        // 현재 인원이 목표 인원보다 많으면 차이만큼 해제 시도
        long difference = currentHeadcount - targetHeadcount;
        if (difference <= 0) {
            return;
        }

        for (int i = 0; i < difference; i++) {
            try {
                courseCapacityCache.release(lectureId);
            } catch (CourseCapacityCacheException ex) {
                log.error("Redis 정원 해제 재시도 중 예외가 발생했습니다. lectureId={}", lectureId, ex);
                break;
            }
        }
    }
}
