package org.hyejoon.cuvcourse.domain.lecture.repository;

import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureJpaRepository extends JpaRepository<Lecture, Long> {

    @Query(value = "SELECT * FROM lectures WHERE MATCH(lecture_title) AGAINST(:keyword IN BOOLEAN MODE)", nativeQuery = true)
    Page<Lecture> searchByTitle(Pageable pageable,@Param("keyword") String keyword);

    Page<Lecture> findAllByLectureTitleContaining(String keyword, Pageable pageable);
}
