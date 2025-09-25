package org.hyejoon.cuvcourse.domain.course.repository;

import java.util.Optional;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJpaRepository extends JpaRepository<Course, CourseId> {

    Optional<Course> findByLectureAndStudent(Long lectureId, Long studentId);
}
