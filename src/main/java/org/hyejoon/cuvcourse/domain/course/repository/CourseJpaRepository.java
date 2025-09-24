package org.hyejoon.cuvcourse.domain.course.repository;

import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJpaRepository extends JpaRepository<Course, Long> {

}
