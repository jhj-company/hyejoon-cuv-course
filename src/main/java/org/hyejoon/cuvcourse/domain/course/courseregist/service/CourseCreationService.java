package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.cache.LectureCacheService;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseCreationService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureCacheService lectureCacheService;

    @Transactional
    public Course createCourseIfAvailable(long lectureId, Student student) {
        Lecture lecture = lectureCacheService.getLectureById(lectureId);

        // 정원 초과 금지
        lecture.validateCapacity();

        CourseId courseId = CourseId.of(lecture, student);

        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(CourseRegistExceptionEnum.ALREADY_REGISTERED);
        }

        Course course = Course.from(courseId);
        Course savedCourse = courseJpaRepository.save(course);
        lectureCacheService.increaseLectureTotal(lecture);
        return savedCourse;
    }
}
