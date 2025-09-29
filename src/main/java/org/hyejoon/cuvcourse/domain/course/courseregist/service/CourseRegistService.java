package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRegistService {

    private static final String COURSE_REGIST_LOCK_KEY = "course-service:regist-lock:";

    private final CourseRegistrationValidator validator;
    private final CourseCreationService courseCreationService;
    private final LockManager lockManager;
    private final DistributedLock distributedLock;

    public CourseResponse registerCourse(long studentId, long lectureId) {
        log.debug("Lock type: {}", distributedLock.getType());

        Student student = validator.getStudent(studentId);
        Lecture lecture = validator.getLecture(lectureId);
        CourseId courseId = CourseId.of(lecture, student);

        String lockKey = COURSE_REGIST_LOCK_KEY + lectureId;

        Course course = lockManager.executeWithLock(distributedLock, lockKey, () -> {
            validator.validateDuplicateRegistration(courseId);
            return courseCreationService.createCourseIfAvailable(lecture, courseId);
        });

        return CourseResponse.from(course);
    }
}
