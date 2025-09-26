package org.hyejoon.cuvcourse.domain.course.create.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.create.exception.CourseCreateExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseRegistService implements CourseRegistUseCase {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final CourseCreationService courseCreationService;

    @Override
    @Transactional
    public CourseResponse registerCourse(long studentId, long lectureId) {
        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(CourseCreateExceptionEnum.STUDENT_NOT_FOUND));
        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(CourseCreateExceptionEnum.LECTURE_NOT_FOUND));

        CourseId courseId = CourseId.of(lecture, student);

        // 중복 신청 금지
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(CourseCreateExceptionEnum.ALREADY_REGISTERED);
        }

        Course course = courseCreationService.createCourseIfAvailable(lecture, courseId);

        return CourseResponse.from(course);
    }
}
