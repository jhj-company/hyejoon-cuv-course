package org.hyejoon.cuvcourse.domain.lecture.repository;

import org.hyejoon.cuvcourse.domain.lecture.document.LectureDocument;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@DisplayName("강의 검색 성능 테스트")
@Disabled("성능 테스트는 수동으로 실행")
public class LectureSearchPerformanceTest {

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    @Autowired
    private LectureESRepository lectureESRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int LECTURE_COUNT = 100_000;
    private static final String SEARCH_KEYWORD = "Perfor";
    private static final int PAGE_SIZE = 100;

    @Test
    void setUp() {
        lectureJpaRepository.deleteAllInBatch();
        lectureESRepository.deleteAll();

        List<Lecture> lecturesToInsert = new ArrayList<>();
        IntStream.range(0, LECTURE_COUNT).forEach(i -> {
            String title = "Lecture " + i;
            if (i == LECTURE_COUNT / 2) {
                title = "Special Lecture on " + SEARCH_KEYWORD + " Testing";
            }
            lecturesToInsert.add(new Lecture(title, "Professor " + i, 3, 100, 0));
        });

        // JDBC Bulk Insert
        String sql = "INSERT INTO lectures (lecture_title, professor_name, credits, capacity, total, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.batchUpdate(sql, lecturesToInsert, 1000,
            (PreparedStatement ps, Lecture lecture) -> {
                ps.setString(1, lecture.getLectureTitle());
                ps.setString(2, lecture.getProfessorName());
                ps.setInt(3, lecture.getCredits());
                ps.setInt(4, lecture.getCapacity());
                ps.setInt(5, lecture.getTotal());
            });

        // 페이징 처리로 메모리 부하 없이 ES에 데이터 색인
        Pageable pageable = PageRequest.of(0, 1000); // 1000개씩 처리
        Page<Lecture> lecturePage;
        do {
            lecturePage = lectureJpaRepository.findAll(pageable);
            List<LectureDocument> documents = lecturePage.getContent().stream()
                .map(LectureDocument::from)
                .collect(Collectors.toList());

            if (!documents.isEmpty()) {
                lectureESRepository.saveAll(documents);
            }

            pageable = lecturePage.nextPageable();
        } while (lecturePage.hasNext());
        assertTrue(true);
    }

    @Test
    @DisplayName("DB LIKE 검색 성능 측정")
    void dbLikeSearchPerformanceTest() {
        StopWatch stopWatch = new StopWatch();
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        stopWatch.start();
        lectureJpaRepository.findByLectureTitleContaining(SEARCH_KEYWORD, pageable);
        stopWatch.stop();

        System.out.println("DB LIKE search time: " + stopWatch.getTotalTimeMillis() + "ms");
    }

    @Test
    @DisplayName("Elasticsearch 검색 성능 측정")
    void elasticsearchSearchPerformanceTest() {
        StopWatch stopWatch = new StopWatch();
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        stopWatch.start();
        lectureESRepository.searchByTitle(SEARCH_KEYWORD, pageable);
        stopWatch.stop();

        System.out.println("Elasticsearch search time: " + stopWatch.getTotalTimeMillis() + "ms");
    }
}
