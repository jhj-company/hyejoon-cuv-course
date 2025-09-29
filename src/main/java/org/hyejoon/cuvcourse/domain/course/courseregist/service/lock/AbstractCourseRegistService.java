package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock;

import java.util.Random;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseCreationService;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistUseCase;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistrationValidator;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCourseRegistService implements CourseRegistUseCase {

    private static final String COURSE_REGIST_LOCK_KEY = "course-service:regist-lock:";

    private final CourseRegistrationValidator validator;
    private final CourseCreationService courseCreationService;
    private final DistributedLock distributedLock;
    private final LockManager lockManager;

    @Override
    public CourseResponse registerCourse(long studentId, long lectureId) {
        log.debug("Lock type: {}", distributedLock.getType());

        Student student = validator.getStudent(studentId);
        Lecture lecture = validator.getLecture(lectureId);
        CourseId courseId = CourseId.of(lecture, student);

        String lockKey = COURSE_REGIST_LOCK_KEY + lectureId;

        // 테스트 환경에서 동시성 시뮬레이션을 위함
        // Prod에서 빼야함!!!
        simulateDelay();

        Course course = lockManager.executeWithLock(distributedLock, lockKey,
            () -> {
                validator.validateDuplicateRegistration(courseId);
                return courseCreationService.createCourseIfAvailable(lecture, courseId);
            }
        );

        return CourseResponse.from(course);
    }

    private void simulateDelay() {
        try {
            int delay = 50 + new Random().nextInt(51); // 50~100ms 랜덤 지연
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}