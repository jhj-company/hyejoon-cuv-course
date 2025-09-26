package org.hyejoon.cuvcourse.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateService;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.lock.LockService;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyTest {

    @Autowired
    CourseCreateService courseCreateService;

    @Autowired
    LockService lockService;

    @Autowired
    CourseJpaRepository courseJpaRepository;

    @Autowired
    StudentJpaRepository studentJpaRepository;

    @Autowired
    LectureJpaRepository lectureJpaRepository;

    private Long studentId;
    private Long lectureId;

    @BeforeEach
    @Transactional
    void setup()
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<Student> studentCons = Student.class.getDeclaredConstructor(
            Long.class, String.class, String.class, String.class, int.class
        );
        studentCons.setAccessible(true);
        Student student = studentCons.newInstance(
            1L, "홍길동", "hong@test.com", "password123", 10
        );

        Constructor<Lecture> lectureCons = Lecture.class.getDeclaredConstructor(
            String.class, String.class, int.class, int.class
        );
        lectureCons.setAccessible(true);
        Lecture lecture = lectureCons.newInstance("자바 동시성", "홍교수", 3, 30);

        studentJpaRepository.save(student);
        lectureJpaRepository.save(lecture);

        studentId = student.getId();
        lectureId = lecture.getId();
    }

    @Test
    @DisplayName("동일 학생-강의 조합에 대한 동시 수강신청은 단일 승인만 허용")
    void concurrentCourseCreateTest() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            results.add(executorService.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                courseCreateService.createCourseWithLock(studentId, lectureId);
                return true;
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        int successCnt = 0;
        int failedCnt = 0;

        for (Future<Boolean> result : results) {
            try {
                result.get();
                successCnt++;
            } catch (ExecutionException e) {
                failedCnt++;
            }
        }

        System.out.println("성공 스레드 수: " + successCnt);
        System.out.println("실패 스레드 수: " + failedCnt);

        assertEquals(successCnt, 1);
        assertEquals(failedCnt, threadCount - 1);
    }
}
