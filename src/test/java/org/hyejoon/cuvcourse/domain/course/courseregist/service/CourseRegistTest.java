package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.CourseCapacitySnapshot;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.cousecancel.service.CourseCancelService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestCourseRegistConfig.class)
public class CourseRegistTest extends RedisTestContainers {

    @Autowired
    // courseRegistNoLockService - 무조건 실패
    // courseRegistSpinLockService - 성공
    // courseRegistPubSubLockService - 성공
    // courseRegistRedissonService - 성공
    @Qualifier("courseRegistRedissonService")
    private CourseRegistService courseRegistService;

    @Autowired
    private StudentJpaRepository studentJpaRepository;

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    @Autowired
    private CourseJpaRepository courseJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Long> courseCapacityRedisTemplate;

    @Autowired
    private CourseCancelService courseCancelService;

    @Autowired
    private CourseCapacityCache courseCapacityCache;

    @BeforeEach
    void clearRedisBefore() {
        Set<String> keys = courseCapacityRedisTemplate.keys("course:lecture:*");
        if (!keys.isEmpty()) {
            courseCapacityRedisTemplate.delete(keys);
        }
    }

    @AfterEach
    void tearDown() {
        courseJpaRepository.deleteAllInBatch();
        studentJpaRepository.deleteAllInBatch();
        lectureJpaRepository.deleteAllInBatch();

        clearRedisBefore();
    }

    @Test
    void 수강신청_동시에_신청해도_정원과_신청수가_일치해야한다() throws Exception {
        // Given

        // 강의 정원
        final int CAPACITY = 30;
        // 해당 강의를 신청하는 학생 수
        final int TOTAL_STUDENT = 150;

        Lecture lecture = new Lecture("강의", "교수님", 3, CAPACITY);
        final Lecture savedLecture = lectureJpaRepository.save(lecture);

        // JdbcTemplate을 사용한 배치 삽입
        String sql = "INSERT INTO students (name, email, password, available_credits, created_at) VALUES (?, ?, ?, ?, NOW())";
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 1; i <= TOTAL_STUDENT; i++) {
            batchArgs.add(new Object[]{"Student" + i, "student" + i + "@test.com", "password", 10});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);

        List<Student> students = studentJpaRepository.findAll();

        // When
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_STUDENT);
        AtomicInteger successCount = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(TOTAL_STUDENT);

        for (Student student : students) {
            executor.submit(() -> {
                try {
                    barrier.await();  // 모든 스레드가 동시에 시작하도록 기다림
                    courseRegistService.registerCourse(student.getId(), savedLecture.getId());
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                    // handle exception
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        int actualSuccessCount = successCount.get();
        assertThat(actualSuccessCount).isEqualTo(CAPACITY);
    }

    @Test
    void 정원_초과시_비즈니스_예외를_반환해야_한다() {
        // Given
        Lecture lecture = new Lecture("강의", "교수", 3, 2);
        Lecture savedLecture = lectureJpaRepository.save(lecture);

        Student firstStudent = insertStudent("first", "first@test.com");
        Student secondStudent = insertStudent("second", "second@test.com");
        Student thirdStudent = insertStudent("third", "third@test.com");

        // When
        courseRegistService.registerCourse(firstStudent.getId(), savedLecture.getId());
        courseRegistService.registerCourse(secondStudent.getId(), savedLecture.getId());

        // Then
        assertThatThrownBy(
            () -> courseRegistService.registerCourse(thirdStudent.getId(), savedLecture.getId())
        )
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(CourseRegistExceptionEnum.CAPACITY_FULL.getMessage());

        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(savedLecture.getId());
        assertThat(snapshot.headcount()).isEqualTo(2);
    }

    @Test
    void 수강취소_이후에도_재등록이_가능해야_한다() {
        // Given
        Lecture lecture = new Lecture("강의", "교수", 3, 1);
        Lecture savedLecture = lectureJpaRepository.save(lecture);

        Student firstStudent = insertStudent("first-cancel", "first-cancel@test.com");
        Student secondStudent = insertStudent("second-cancel", "second-cancel@test.com");

        courseRegistService.registerCourse(firstStudent.getId(), savedLecture.getId());

        // When
        courseCancelService.courseCancel(savedLecture.getId(), firstStudent.getId());

        courseRegistService.registerCourse(secondStudent.getId(), savedLecture.getId());

        // Then
        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(savedLecture.getId());
        assertThat(snapshot.headcount()).isEqualTo(1);
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
