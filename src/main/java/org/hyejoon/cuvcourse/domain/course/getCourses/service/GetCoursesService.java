package org.hyejoon.cuvcourse.domain.course.getCourses.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.getCourses.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.getCourses.dto.GetCoursesResponse;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.login.exception.StudentExceptionEnum;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCoursesService {

    private final CourseJpaRepository courseJpaRepository;
    private final StudentJpaRepository studentJpaRepository;

    @Transactional(readOnly = true)
    public GetCoursesResponse getCourses(Long studentId) {
        List<Course> courses = courseJpaRepository.findByStudentIdWithLecture(studentId);

        if (courses.isEmpty()) {
            Student student = studentJpaRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(
                    StudentExceptionEnum.STUDENT_NOT_FOUND));
            return new GetCoursesResponse(List.of(), 0, student.getAvailableCredits());
        }

        List<CourseResponse> courseResponses = courses.stream().map(course -> {
            Lecture lecture = course.getId().getLecture();
            return new CourseResponse(
                lecture.getId(),
                lecture.getLectureTitle(),
                lecture.getProfessorName(),
                lecture.getCredits()
            );
        }).toList();

        int enrolledCredits = courseResponses.stream()
            .mapToInt(CourseResponse::credits)
            .sum();

        Student student = courses.get(0).getId().getStudent();
        int availableCredits = student.getAvailableCredits();

        return new GetCoursesResponse(courseResponses, enrolledCredits, availableCredits);
    }
}
