package org.hyejoon.cuvcourse.domain.course.create.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.create.exception.CourseCreateExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.redis.SpinLockService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseRegistSpinLockService implements CourseRegistUseCase {

    private static final int LOCK_ACQUIRE_RETRY_DELAY_MS = 1000;
    private static final int LOCK_ACQUIRE_MAX_RETRY = 3;
    private static final long LOCK_TIMEOUT_SECONDS = 10L;
    private static final String COURSE_CREATE_LOCK_KEY = "course:create:lock:";

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final SpinLockService spinLockService;
    private final CourseCreationService courseCreationService;

    @Override
    public CourseResponse registerCourse(long studentId, long lectureId) {
        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(CourseCreateExceptionEnum.STUDENT_NOT_FOUND));
        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(CourseCreateExceptionEnum.LECTURE_NOT_FOUND));

        CourseId courseId = CourseId.of(lecture, student);

        checkDuplicateRegistration(courseId);

        String lockKey = COURSE_CREATE_LOCK_KEY + lectureId;
        Boolean lockAcquired = acquireLockWithRetry(lockKey);

        try {
            Course savedCourse = courseCreationService.createCourseIfAvailable(lecture, courseId);
            return CourseResponse.from(savedCourse);
        } finally {
            if (lockAcquired != null && lockAcquired) {
                spinLockService.releaseLock(lockKey);
            }
        }
    }

    private void checkDuplicateRegistration(CourseId courseId) {
        // 중복 신청 금지
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(CourseCreateExceptionEnum.ALREADY_REGISTERED);
        }
    }

    private Boolean acquireLockWithRetry(String lockKey) {
        try {
            for (int i = 0; i < LOCK_ACQUIRE_MAX_RETRY; i++) {
                Boolean lockAcquired = spinLockService.acquireLock(lockKey, LOCK_TIMEOUT_SECONDS);
                if (lockAcquired != null && lockAcquired) {
                    return lockAcquired;
                }
                Thread.sleep(LOCK_ACQUIRE_RETRY_DELAY_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new BusinessException(CourseCreateExceptionEnum.LOCK_ACQUIRE_FAILED);
    }
}
