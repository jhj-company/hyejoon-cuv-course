package org.hyejoon.cuvcourse.domain.course.create.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateService;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CourseCreateServiceTest {

    @Autowired
    private CourseCreateService courseCreateService;
    @Autowired
    private CourseJpaRepository courseJpaRepository;
    @Autowired
    private StudentJpaRepository studentJpaRepository;
    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    private Long lectureId;
    private List<Student> students;

    @BeforeEach
    void setup() {
        // 이전 테스트 데이터를 모두 삭제하여 독립적인 테스트 환경 보장
        courseJpaRepository.deleteAllInBatch();
        studentJpaRepository.deleteAllInBatch();
        lectureJpaRepository.deleteAllInBatch();

        // 1. 테스트용 강의 1개 생성 (정원 10명)
        Lecture lecture = Lecture.builder()
            .lectureTitle("테스트 강의")
            .professorName("교수님")
            .credits(3)
            .capacity(10)
            .build();
        Lecture savedLecture = lectureJpaRepository.save(lecture);
        this.lectureId = savedLecture.getId(); // DB가 생성해준 실제 ID를 사용

        // 2. 테스트용 학생 100명 생성
        List<Student> studentList = LongStream.rangeClosed(1, 100)
            .mapToObj(i -> Student.builder()
                .name("학생" + i)
                .email("s" + i + "@test.com")
                .password("1234")
                .availableCredits(21)
                .build())
            .collect(Collectors.toList());
        this.students = studentJpaRepository.saveAll(studentList); // DB에 저장된 실제 학생 목록을 사용
    }

    @Test
    @DisplayName("성공: 100명의 학생이 동시에 수강신청을 해도 정원(10명)을 넘지 않는다")
    void 수강신청_동시성_테스트() throws InterruptedException {
        // Given
        final int studentCount = this.students.size();
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch latch = new CountDownLatch(studentCount);

        // When
        for (Student student : this.students) {
            executorService.submit(() -> {
                try {
                    // setup에서 준비된 실제 학생 ID와 강의 ID를 사용
                    courseCreateService.createCourse(student.getId(), this.lectureId);
                } catch (Exception e) {
                    // 정원 초과, 중복 신청 등의 예외는 정상 동작으로 간주
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); // 모든 스레드의 작업이 끝날 때까지 대기
        executorService.shutdown();

        // Then
        long finalHeadcount = courseJpaRepository.countByIdLectureId(this.lectureId);
        assertThat(finalHeadcount).isEqualTo(10); // 최종 신청 인원은 10명이어야 한다.
    }
}