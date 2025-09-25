package org.hyejoon.cuvcourse.domain.lecture.repository;

import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureJpaRepository extends JpaRepository<Lecture, Long>, LectureRepositoryCustom {

}
