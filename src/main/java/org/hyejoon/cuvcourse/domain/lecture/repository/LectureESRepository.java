package org.hyejoon.cuvcourse.domain.lecture.repository;

import org.hyejoon.cuvcourse.domain.lecture.document.LectureDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LectureESRepository extends ElasticsearchRepository<LectureDocument, String> {

    @Query("{\"match\": {\"lectureTitle\": \"?0\"}}")
    Page<LectureDocument> searchByTitle(String keyword, Pageable pageable);

    Page<LectureDocument> findByLectureTitleContaining(String keyword, Pageable pageable);
}
