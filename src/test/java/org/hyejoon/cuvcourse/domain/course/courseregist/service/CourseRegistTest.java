package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
public class CourseRegistTest {

    @Autowired
    // courseRegistService - 무조건 실패
    // courseRegistSpinLockService - 성공
    // courseRegistPubSubLockService - 성공
    // courseRegistRedissonService - 성공
    @Qualifier("courseRegistRedissonService")
    private CourseRegistUseCase courseCreateService;

    @Autowired
    private StudentJpaRepository studentJpaRepository;

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    private Student createStudent(String name, String email, String password,
        int credits) throws Exception {
        Constructor<Student> constructor = Student.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Student student = constructor.newInstance();
        ReflectionTestUtils.setField(student, "name", name);
        ReflectionTestUtils.setField(student, "email", email);
        ReflectionTestUtils.setField(student, "password", password);
        ReflectionTestUtils.setField(student, "availableCredits", credits);
        return student;
    }

    @Test
    void 수강신청_동시에_신청해도_정원과_신청수가_일치해야한다() throws Exception {
        // Given

        // 강의 정원
        final int CAPACITY = 30;
        // 해당 강의를 신청하는 학생 수
        final int TOTAL_STUDENT = 60;

        Lecture lecture = new Lecture("강의", "교수님", 3, CAPACITY);
        final Lecture savedLecture = lectureJpaRepository.save(lecture);

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= TOTAL_STUDENT; i++) {
            Student student = createStudent("Student" + i, "student" + i + "@test.com",
                "password", 10);
            students.add(studentJpaRepository.save(student));
        }

        // When
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_STUDENT);
        AtomicInteger successCount = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(TOTAL_STUDENT);

        for (Student student : students) {
            executor.submit(() -> {
                try {
                    barrier.await();  // 모든 스레드가 동시에 시작하도록 기다림
                    courseCreateService.registerCourse(student.getId(), savedLecture.getId());
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                    // handle exception
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        int actualSuccessCount = successCount.get();
        assertThat(actualSuccessCount).isEqualTo(CAPACITY);
    }
}
