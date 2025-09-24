package org.hyejoon.cuvcourse.domain.student.repository;

import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentJpaRepository extends JpaRepository<Student, Long> {

}
