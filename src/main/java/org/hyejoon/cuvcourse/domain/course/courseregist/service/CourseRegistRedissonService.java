package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import java.util.concurrent.TimeUnit;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Primary
@Service
@RequiredArgsConstructor
public class CourseRegistRedissonService implements CourseRegistUseCase {

    private static final int LOCK_LEASE_SECONDS = 3;
    private static final int LOCK_MAX_WAIT_SECONDS = 3;
    private static final String COURSE_REGIST_LOCK_KEY = "course-service:regist-lock:";

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final CourseCreationService courseCreationService;
    private final RedissonClient redissonClient;

    @Override
    public CourseResponse registerCourse(long studentId, long lectureId) {
        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(CourseRegistExceptionEnum.STUDENT_NOT_FOUND));
        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(CourseRegistExceptionEnum.LECTURE_NOT_FOUND));

        CourseId courseId = CourseId.of(lecture, student);

        checkDuplicateRegistration(courseId);

        RLock lock = redissonClient.getLock(COURSE_REGIST_LOCK_KEY + lectureId);

        try {
            if (acquireLock(lock)) {
                Course course = courseCreationService.createCourseIfAvailable(lecture, courseId);
                return CourseResponse.from(course);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        throw new BusinessException(CourseRegistExceptionEnum.LOCK_ACQUIRE_FAILED);
    }

    private void checkDuplicateRegistration(CourseId courseId) {
        // 중복 신청 금지
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(CourseRegistExceptionEnum.ALREADY_REGISTERED);
        }
    }

    private boolean acquireLock(RLock lock) {
        try {
            return lock.tryLock(LOCK_MAX_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CourseRegistExceptionEnum.LOCK_ACQUIRE_FAILED);
        }
    }
}
