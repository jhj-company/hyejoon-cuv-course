package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseCreationService;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistUseCase;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistrationValidator;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.lock.DistributedLock;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.global.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCourseRegistService implements CourseRegistUseCase {

    protected static final String COURSE_REGIST_LOCK_KEY = "course-service:regist-lock:";
    private static final int LOCK_ACQUIRE_RETRY_DELAY_MS = 1000;
    private static final int LOCK_ACQUIRE_MAX_RETRY = 3;

    protected final CourseRegistrationValidator validator;
    protected final CourseCreationService courseCreationService;
    protected final DistributedLock distributedLock;

    @Override
    public CourseResponse registerCourse(long studentId, long lectureId) {
        log.debug("Lock type: {}", distributedLock.getType());

        Student student = validator.getStudent(studentId);
        Lecture lecture = validator.getLecture(lectureId);

        CourseId courseId = CourseId.of(lecture, student);

        validator.validateDuplicateRegistration(courseId);

        String lockKey = COURSE_REGIST_LOCK_KEY + lectureId;

        if (acquireLockWithRetry(lockKey)) {
            try {
                try {
                    Thread.sleep(100); // 100ms 정도의 지연을 주어 실제 운영 환경처럼 처리함
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Course course = courseCreationService.createCourseIfAvailable(lecture, courseId);
                return CourseResponse.from(course);
            } finally {
                distributedLock.releaseLock(lockKey);
            }
        }

        throw new BusinessException(CourseRegistExceptionEnum.LOCK_ACQUIRE_FAILED);
    }

    private boolean acquireLockWithRetry(String lockKey) {
        try {
            for (int i = 0; i < LOCK_ACQUIRE_MAX_RETRY; i++) {
                if (distributedLock.acquireLock(lockKey)) {
                    return true;
                }
                Thread.sleep(LOCK_ACQUIRE_RETRY_DELAY_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CourseRegistExceptionEnum.LOCK_ACQUIRE_FAILED);
        }
        return false;
    }
}