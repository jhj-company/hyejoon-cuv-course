package org.hyejoon.cuvcourse.domain.lecture.repository;

import org.hyejoon.cuvcourse.domain.lecture.lectureView.dto.LectureViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LectureRepositoryCustom {
    Page<LectureViewResponse> findLecturesWithTotal(Pageable pageable);
}
