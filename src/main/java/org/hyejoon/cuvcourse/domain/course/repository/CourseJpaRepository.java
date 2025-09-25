package org.hyejoon.cuvcourse.domain.course.repository;

import java.util.List;
import java.util.Optional;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseJpaRepository extends JpaRepository<Course, CourseId> {

    @Query("SELECT c FROM Course c JOIN FETCH c.id.lecture WHERE c.id.student.id = :studentId")
    List<Course> findByStudentIdWithLecture(@Param("studentId") Long studentId);

    @Query("SELECT c FROM Course c WHERE c.id.lecture.id = :lectureId AND c.id.student.id = :studentId")
    Optional<Course> findByLectureAndStudent(
        @Param("lectureId") Long lectureId,
        @Param("studentId") Long studentId
    );
}
