package org.hyejoon.cuvcourse.domain.lecture.lecturesearch.controller;

import org.hyejoon.cuvcourse.domain.lecture.lecturesearch.dto.LectureSearchRequest;
import org.hyejoon.cuvcourse.domain.lecture.lecturesearch.dto.LectureSearchResponse;
import org.hyejoon.cuvcourse.domain.lecture.lecturesearch.service.LectureSearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureSearchController {

    private final LectureSearchService lectureSearchService;

    @PostMapping("/search")
    public Page<LectureSearchResponse> searchLectures(@RequestBody LectureSearchRequest request) {
        return lectureSearchService.searchByTitle(request.title(), request.page(), request.size());
    }
}
