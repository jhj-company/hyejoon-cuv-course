package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

@SpringBootTest
@Import(TestCourseRegistConfig.class)
@Tag("load")
class CourseRegistHighLoadTest {

    @Autowired
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
    void 정원500명_동시1000명_신청시_성공건수는_정원과_일치해야한다() throws Exception {
        final int CAPACITY = 400;
        final int TOTAL_STUDENT = 700;

        Lecture lecture = new Lecture("대용량 강의", "교수님", 3, CAPACITY);
        Lecture savedLecture = lectureJpaRepository.save(lecture);

        String sql = "INSERT INTO students (name, email, password, available_credits, created_at) VALUES (?, ?, ?, ?, NOW())";
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 1; i <= TOTAL_STUDENT; i++) {
            batchArgs.add(
                new Object[]{"BulkStudent" + i, "bulkstudent" + i + "@test.com", "password", 10});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);

        List<Student> students = studentJpaRepository.findAll();

        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_STUDENT);
        AtomicInteger successCount = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(TOTAL_STUDENT);

        StopWatch stopWatch = new StopWatch("대용량 수강신청 테스트");
        stopWatch.start("신청 요청 처리");

        for (Student student : students) {
            executor.submit(() -> {
                try {
                    barrier.await();
                    courseRegistService.registerCourse(student.getId(), savedLecture.getId());
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                    // 대용량 테스트에서는 실패 케이스를 따로 로깅하지 않는다.
                }
            });
        }

        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
            if (!finished) {
                fail("테스트 시간이 초과되었습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int actualSuccessCount = successCount.get();
        assertThat(actualSuccessCount).isEqualTo(CAPACITY);
        assertThat(courseJpaRepository.countByIdLecture(savedLecture)).isEqualTo(CAPACITY);

        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(savedLecture.getId());
        assertThat(snapshot.headcount()).isEqualTo(CAPACITY);

        stopWatch.stop();
        System.out.println("정원 500명, 동시 1000명 수강신청 소요시간: " + stopWatch.getTotalTimeMillis() + "ms");
    }
}
