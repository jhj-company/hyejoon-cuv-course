package org.hyejoon.cuvcourse.domain.lecture.lectureView.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.lecture.lectureView.dto.LectureViewResponse;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureViewService {

    private final LectureJpaRepository lectureJpaRepository;

    @Transactional(readOnly = true)
    public Page<LectureViewResponse> findAll(Pageable pageable) {

        return lectureJpaRepository.findLecturesWithTotal(pageable);
    }
}
