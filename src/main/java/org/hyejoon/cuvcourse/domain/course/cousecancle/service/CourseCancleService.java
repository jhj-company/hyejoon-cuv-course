package org.hyejoon.cuvcourse.domain.course.cousecancle.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.cousecancle.exception.CourseCancleExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCancleService {

    private final CourseJpaRepository courseJpaRepository;

    @Transactional
    public void courseCancle(Long lectureId, Long studentId) {
        Course course = courseJpaRepository.findByLectureAndStudent(lectureId, studentId)
            .orElseThrow(() -> new BusinessException(CourseCancleExceptionEnum.COURSE_NOT_FOUND));

        courseJpaRepository.delete(course);
    }
}
