package org.hyejoon.cuvcourse.domain.course.cousecancel.service;

import lombok.RequiredArgsConstructor;

import org.hyejoon.cuvcourse.domain.course.cousecancel.event.CourseCancelCompensationEvent;
import org.hyejoon.cuvcourse.domain.course.cousecancel.exception.CourseCancelExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.cache.LectureCacheService;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.event.LectureUpdatedEvent;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCancelService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureCacheService lectureCacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void courseCancel(Long lectureId, Long studentId) {
        Course course = courseJpaRepository.findByLectureAndStudent(lectureId, studentId)
            .orElseThrow(() -> new BusinessException(CourseCancelExceptionEnum.COURSE_NOT_FOUND));

        Lecture lecture = course.getId().getLecture();
        lectureCacheService.decreaseLectureTotal(lecture);
        eventPublisher.publishEvent(new CourseCancelCompensationEvent(lecture.getId()));
        eventPublisher.publishEvent(new LectureUpdatedEvent(lectureId));

        courseJpaRepository.delete(course);
    }
}
