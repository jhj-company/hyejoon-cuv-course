package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRegistWithoutCacheService {

    private static final String COURSE_REGIST_LOCK_KEY = "course-service:regist-lock:";

    private final CourseCreationWithoutCacheService courseCreationService;
    private final LockManager lockManager;
    private final DistributedLock distributedLock;
    private final StudentJpaRepository studentJpaRepository;

    public CourseResponse registerCourse(long studentId, long lectureId) {
        log.debug("Lock type: {}", distributedLock.getType());

        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(
                CourseRegistExceptionEnum.STUDENT_NOT_FOUND));
        String lockKey = COURSE_REGIST_LOCK_KEY + lectureId;

        Course course = lockManager.executeWithLock(distributedLock, lockKey,
            () -> courseCreationService.registerCourseIfAvailable(lectureId, student)
        );

        return CourseResponse.from(course);
    }
}
