package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
public class CourseRegistCacheTest {

    // 강의 정원
    private final int CAPACITY = 500;
    // 해당 강의를 신청하는 학생 수
    private final int TOTAL_STUDENT = 1000;
    @Autowired
    private CourseRegistService courseRegistService;
    @Autowired
    private CourseCacheService courseCacheService;
    @Autowired
    private LectureJpaRepository lectureJpaRepository;
    @Autowired
    private StudentJpaRepository studentJpaRepository;
    @Autowired
    private CourseJpaRepository courseJpaRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;
    private Lecture lecture;

    @BeforeEach
    void setUp() {
        // 강의 생성
        lecture = lectureJpaRepository.save(new Lecture("테스트 강의", "교수님", 3, CAPACITY));
        // 학생 생성
        String sql = "INSERT INTO students (name, email, password, available_credits, created_at) VALUES (?, ?, ?, ?, NOW())";

        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 1; i <= TOTAL_STUDENT; i++) {
            batchArgs.add(new Object[]{"Student" + i, "student" + i + "@test.com", "password", 10});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @AfterEach
    void tearDown() {
        // Redis 캐시 초기화
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void 동시에_수강신청_시_총원_정합성() throws InterruptedException {
        List<Student> students = studentJpaRepository.findAll();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(students.size());

        long startTime = System.currentTimeMillis();

        students.forEach(student -> executor.submit(() -> {
            try {
                courseRegistService.registerCourse(student.getId(), lecture.getId());
            } catch (BusinessException e) {
                // 정원 초과 또는 중복 등록 예외는 무시하고 로그만
                System.out.println("Exception: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        }));

        latch.await();
        executor.shutdown();

        long totalDuration = System.currentTimeMillis() - startTime;
        System.out.println("총 수강신청 처리 시간: " + totalDuration + "ms");

        // DB에서 실제 수강 인원 확인
        long dbHeadcount = courseJpaRepository.countByIdLecture(lecture);
        System.out.println("DB Headcount = " + dbHeadcount);
        assertThat(dbHeadcount).isEqualTo(CAPACITY); // 강의 정원이 2이므로

        // 캐시에서도 동일하게 반영되었는지 확인
        long cachedHeadcount = courseCacheService.getCurrentHeadcount(lecture);
        System.out.println("Cached Headcount = " + cachedHeadcount);
        assertThat(cachedHeadcount).isEqualTo(dbHeadcount);
    }
}
