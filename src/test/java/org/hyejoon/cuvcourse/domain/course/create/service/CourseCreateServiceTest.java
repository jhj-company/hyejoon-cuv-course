package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
public class CourseCreateServiceTest {

    @Autowired
    private CourseCreateService courseCreateService;

    @Autowired
    private StudentJpaRepository studentJpaRepository;

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    private Student createStudent(Long id, String name, String email, String password,
        int credits) throws Exception {
        Constructor<Student> constructor = Student.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Student student = constructor.newInstance();
        ReflectionTestUtils.setField(student, "id", id);
        ReflectionTestUtils.setField(student, "name", name);
        ReflectionTestUtils.setField(student, "email", email);
        ReflectionTestUtils.setField(student, "password", password);
        ReflectionTestUtils.setField(student, "availableCredits", credits);
        return student;
    }

    @Test
    void 수강신청_동시에_신청해도_정원과_신청수가_일치해야한다() throws Exception {
        // Given: 강의 (정원 20)
        Lecture lecture = new Lecture("강의", "교수님", 3, 20);
        final Lecture savedLecture = lectureJpaRepository.save(lecture);

        // Given: 학생들 (100명)
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Student student = createStudent((long) i, "Student" + i, "student" + i + "@test.com",
                "password", 10);
            students.add(studentJpaRepository.save(student));
        }

        // When
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger successCount = new AtomicInteger(0);

        List<Future<Void>> futures = new ArrayList<>();
        for (Student student : students) {
            Future<Void> future = executor.submit(() -> {
                try {
                    courseCreateService.createCourse(student.getId(), savedLecture.getId());
                    successCount.incrementAndGet();
                } catch (BusinessException ignored) {

                }
                return null;
            });
            futures.add(future);
        }

        for (Future<Void> future : futures) {
            future.get();
        }
        executor.shutdown();

        // Then
        int actualSuccessCount = successCount.get();
        assertThat(actualSuccessCount).isEqualTo(20);
    }
}
