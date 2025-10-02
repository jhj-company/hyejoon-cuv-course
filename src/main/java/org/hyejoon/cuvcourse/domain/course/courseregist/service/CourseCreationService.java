package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseCreationService {

    private final CourseJpaRepository courseJpaRepository;

    @Transactional
    public Course createCourseIfAvailable(Lecture lecture, CourseId courseId) {
        long currentHeadcount = courseJpaRepository.countByIdLecture(lecture);

        // 정원 초과 금지
        if (currentHeadcount >= lecture.getCapacity()) {
            throw new BusinessException(CourseRegistExceptionEnum.CAPACITY_FULL);
        }

        Course course = Course.from(courseId);
        return courseJpaRepository.save(course);
    }
}
