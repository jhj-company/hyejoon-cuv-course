package org.hyejoon.cuvcourse.domain.lecture.view.service;

import org.hyejoon.cuvcourse.domain.lecture.lectureView.service.LectureSearchTestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LectureSearchSpeedTest {

    @Autowired
    private LectureSearchTestService lectureSearchTestService;

    @Test
    @DisplayName("LIKE 와 풀텍스트 검색 비교")
    void fullTextVsLikeSearchSpeedTest() {
        lectureSearchTestService.testSearchSpeed("Java");
    }
}
