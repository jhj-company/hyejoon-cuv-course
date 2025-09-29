package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseCreationService {

    private final CourseJpaRepository courseJpaRepository;

    @Transactional
    public Course createCourseIfAvailable(Lecture lecture, CourseId courseId) {

        // 정원 초과 금지
        lecture.validateCapacity();

        Course course = Course.from(courseId);
        lecture.increaseTotal();
        return courseJpaRepository.save(course);
    }
}
