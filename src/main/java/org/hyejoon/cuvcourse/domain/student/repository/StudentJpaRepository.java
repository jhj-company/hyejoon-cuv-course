package org.hyejoon.cuvcourse.domain.student.repository;

import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface StudentJpaRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
}
