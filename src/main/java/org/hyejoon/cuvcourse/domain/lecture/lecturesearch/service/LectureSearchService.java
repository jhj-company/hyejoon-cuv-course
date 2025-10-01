package org.hyejoon.cuvcourse.domain.lecture.lecturesearch.service;

import org.hyejoon.cuvcourse.domain.lecture.document.LectureDocument;
import org.hyejoon.cuvcourse.domain.lecture.lecturesearch.dto.LectureSearchResponse;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureESRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureSearchService {

    private final LectureESRepository lectureESRepository;

    @Transactional(readOnly = true)
    public Page<LectureSearchResponse> searchByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LectureDocument> lectureDocuments = lectureESRepository.searchByTitle(
            keyword, pageable);

        return lectureDocuments.map(LectureSearchResponse::of);
    }
}
