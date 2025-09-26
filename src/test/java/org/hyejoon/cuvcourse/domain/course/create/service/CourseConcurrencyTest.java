package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
public class CourseConcurrencyTest {

    private final int STUDENT_COUNT = 100;
    private final int LECTURE_CAPACITY = 50;

    @Autowired
    private StudentJpaRepository studentJpaRepository;
    @Autowired
    private LectureJpaRepository lectureJpaRepository;
    @Autowired
    private CourseJpaRepository courseJpaRepository;
    @Autowired
    private CourseCreateService courseCreateService;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Lecture lecture;

    @BeforeEach
    public void setup() {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            lecture = lectureJpaRepository.saveAndFlush(
                new Lecture("테스트 강의", "홍길동", 30, LECTURE_CAPACITY)
            );

            LongStream.rangeClosed(1, STUDENT_COUNT).forEach(i -> {
                studentJpaRepository.save(
                    new Student("test" + i, "test" + i + "@test.com", "password123!", 10)
                );
            });

            return null;
        });
    }

    @AfterEach
    void tearDown() {
        courseJpaRepository.deleteAllInBatch();
        studentJpaRepository.deleteAllInBatch();
        lectureJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시 수강신청 요청이 있을 때, 분산 락으로 데이터 일관성을 유지한다")
    public void distributedLockTest() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(STUDENT_COUNT);

        for (long i = 1; i <= STUDENT_COUNT; i++) {
            final long studentId = i;

            executorService.submit(() -> {
                try {
                    // Facade를 통해 분산 락을 적용하여 수강신청 실행
                    courseCreateService.createCourse(studentId, lecture.getId());
                } catch (Exception e) {
                    System.out.println("수강신청 실패(예외 발생): " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long currentHeadcount = courseJpaRepository.countByIdLecture(lecture);
        assertThat(currentHeadcount).isEqualTo(LECTURE_CAPACITY);
    }

}
