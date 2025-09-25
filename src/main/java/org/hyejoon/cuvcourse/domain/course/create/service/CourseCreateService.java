package org.hyejoon.cuvcourse.domain.course.create.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCreateService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;

    @Transactional
    public CourseResponse createCourse(Long studentId, Long lectureId) {
        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("해당 학생을 찾을 수 없습니다." + studentId));
        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다." + lectureId));

        CourseId courseId = CourseId.of(lecture, student);

        if (courseJpaRepository.existsById(courseId)) {
            throw new IllegalStateException("이미 수강신청한 강의 입니다.");
        }
        lecture.addStudent();

        Course course = Course.from(courseId);
        courseJpaRepository.save(course);
        return CourseResponse.from(course);

    }
}
