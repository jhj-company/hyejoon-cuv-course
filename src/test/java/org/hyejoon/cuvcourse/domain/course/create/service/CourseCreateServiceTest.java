package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseCreateServiceTest {

    @Autowired
    private CourseCreateService courseCreateService;

    @Autowired
    private CourseJpaRepository courseJpaRepository;

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    @Autowired
    private StudentJpaRepository studentJpaRepository;

    private Lecture lecture;
    private List<Student> students;

    @BeforeEach
    void setUp() {
        courseJpaRepository.deleteAllInBatch();
        lectureJpaRepository.deleteAllInBatch();
        studentJpaRepository.deleteAllInBatch();

        // 테스트용 강의 생성
        lecture = new Lecture(
            "Test Lecture",
            "Prof. Kim",
            3,
            50
        );
        lecture = lectureJpaRepository.save(lecture);

        students = java.util.stream.IntStream.rangeClosed(1, 300)
            .mapToObj(i -> new Student(
                "Student" + i,
                "student" + i + "@example.com",
                "password" + i,
                10
            ))
            .toList();
        studentJpaRepository.saveAll(students);
    }

    @Test
    void testConcurrentCourseRegistrationWithCyclicBarrier() throws InterruptedException {
        int threadCount = 300;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        Callable<String> task = () -> {
            try {
                barrier.await();
                Student student = students.get((int) (Math.random() * students.size()));
                courseCreateService.createCourse(student.getId(), lecture.getId());
                return "SUCCESS";
            } catch (BusinessException e) {
                return e.getMessage();
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        };

        List<Future<String>> futures = executor.invokeAll(
            java.util.Collections.nCopies(threadCount, task)
        );

        executor.shutdown();

        long successCount = futures.stream().filter(f -> {
            try {
                return "SUCCESS".equals(f.get());
            } catch (Exception e) {
                return false;
            }
        }).count();

        long failureCount = futures.stream().filter(f -> {
            try {
                return !"SUCCESS".equals(f.get());
            } catch (Exception e) {
                return false;
            }
        }).count();

        long totalCourses = courseJpaRepository.countByIdLecture(lecture);

        System.out.println("동시 요청 시도: " + futures.size());
        System.out.println("성공: " + successCount);
        System.out.println("실패: " + failureCount);
        System.out.println("DB에 실제 저장된 수강 인원: " + totalCourses);

        assertThat(totalCourses).isLessThanOrEqualTo(lecture.getCapacity());
    }
}
