package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.event.CourseRegistCompensationEvent;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.cache.LectureCacheService;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.event.LectureUpdatedEvent;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCreationService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureCacheService lectureCacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Course registerCourseIfAvailable(long lectureId, Student student) {
        Lecture lecture = lectureCacheService.getLectureById(lectureId);

        // 정원 초과 금지
        lecture.validateCapacity();

        CourseId courseId = CourseId.of(lecture, student);

        // 중복 신청 금지
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(CourseRegistExceptionEnum.ALREADY_REGISTERED);
        }

        Course course = Course.from(courseId);
        Course savedCourse = courseJpaRepository.save(course);
        lectureCacheService.increaseLectureTotal(lecture);
        eventPublisher.publishEvent(new CourseRegistCompensationEvent(lectureId));
        eventPublisher.publishEvent(new LectureUpdatedEvent(lectureId));
        return savedCourse;
    }
}
