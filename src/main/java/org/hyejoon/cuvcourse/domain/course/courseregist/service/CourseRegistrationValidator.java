package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseRegistrationValidator {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;

    public Student getStudent(long studentId) {
        return studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(CourseRegistExceptionEnum.STUDENT_NOT_FOUND));
    }

    public Lecture getLecture(long lectureId) {
        return lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(CourseRegistExceptionEnum.LECTURE_NOT_FOUND));
    }

    public void validateDuplicateRegistration(CourseId courseId) {
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(CourseRegistExceptionEnum.ALREADY_REGISTERED);
        }
    }
}