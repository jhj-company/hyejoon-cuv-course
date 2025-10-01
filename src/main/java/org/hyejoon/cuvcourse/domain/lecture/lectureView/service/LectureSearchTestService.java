package org.hyejoon.cuvcourse.domain.lecture.lectureView.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureSearchTestService {

    private final LectureJpaRepository lectureJpaRepository;

    public void testSearchSpeed(String keyword) {
        long start, end;

        Pageable pageable = PageRequest.of(0, 100);

        // LIKE 검색
        start = System.currentTimeMillis();
        Page<Lecture> likeResult = lectureJpaRepository.findAllByLectureTitleContaining(keyword, pageable);
        end = System.currentTimeMillis();
        System.out.println("LIKE 검색: " + likeResult.getContent().size() + "건, 시간: " + (end - start) + "ms");

        // Full Text 검색
        start = System.currentTimeMillis();
        Page<Lecture> ftResult   = lectureJpaRepository.searchByTitle(pageable, keyword);
        end = System.currentTimeMillis();
        System.out.println("Full Text 검색: " + ftResult.getContent().size() + "건, 시간: " + (end - start) + "ms");
        }
}
