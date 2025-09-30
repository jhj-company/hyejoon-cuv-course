package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCacheException;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.testinfra.RedisTestContainers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestCourseRegistConfig.class)
class CourseRegistRedisFallbackTest extends RedisTestContainers {

    @MockitoBean
    private CourseCapacityCache courseCapacityCache;

    @Autowired
    private CourseJpaRepository courseJpaRepository;

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    @Autowired
    private StudentJpaRepository studentJpaRepository;

    @Autowired
    @Qualifier("courseRegistRedissonService")
    private CourseRegistService courseRegistService;

    @Autowired
    private CourseRegistTxService courseRegistTxService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Lecture savedLecture;
    private Student firstStudent;
    private Student secondStudent;

    @BeforeEach
    void setUp() {
        when(courseCapacityCache.getOrInit(anyLong()))
            .thenThrow(new CourseCapacityCacheException("Redis 연결이 불안정합니다."));

        savedLecture = lectureJpaRepository.save(new Lecture("폴백강의", "교수A", 3, 1));

        firstStudent = insertStudent("fallback-first", "fallback-first@test.com");
        secondStudent = insertStudent("fallback-second", "fallback-second@test.com");
    }

    @AfterEach
    void tearDown() {
        courseJpaRepository.deleteAllInBatch();
        studentJpaRepository.deleteAllInBatch();
        lectureJpaRepository.deleteAllInBatch();
    }

    @Test
    void redis장애시에도_DB_폴백으로_수강신청을_완료한다() {
        // When
        courseRegistService.registerCourse(firstStudent.getId(), savedLecture.getId());

        // Then
        long savedCount = courseJpaRepository.countByIdLecture(savedLecture);
        assertThat(savedCount).isEqualTo(1);
        verify(courseCapacityCache, never()).tryReserve(anyLong());
    }

    @Test
    void redis장애시에도_DB_폴백으로_정원_초과를_검증한다() {
        // Given
        courseRegistTxService.createCourseIfAvailable(
            savedLecture,
            CourseId.of(savedLecture, firstStudent)
        );

        // When & Then
        assertThatThrownBy(
            () -> courseRegistService.registerCourse(secondStudent.getId(), savedLecture.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(CourseRegistExceptionEnum.CAPACITY_FULL.getMessage());
    }

    private Student insertStudent(String name, String email) {
        jdbcTemplate.update(
            "INSERT INTO students (name, email, password, available_credits, created_at) VALUES (?, ?, ?, ?, NOW())",
            name,
            email,
            "password",
            18
        );

        return studentJpaRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("테스트 학생 생성에 실패했습니다."));
    }
}
