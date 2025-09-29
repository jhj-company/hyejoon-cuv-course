package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.ALREADY_REGISTERED;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCreateService {

    private final CourseJpaRepository courseJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;

    @DistributedLock(key = "'lecture:' + #lectureId")
    @Transactional
    public CourseResponse createCourse(Long studentId, Long lectureId) {
        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(CourseExceptionEnum.STUDENT_NOT_FOUND));

        // --- lectureJpaRepository를 사용하여 잠금 메소드 호출 ---
        Lecture lecture = lectureJpaRepository.findByIdWithPessimisticLock(lectureId)
            .orElseThrow(() -> new BusinessException(CourseExceptionEnum.LECTURE_NOT_FOUND));

        CourseId courseId = CourseId.of(lecture, student);

        // 중복 신청 금지
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(ALREADY_REGISTERED);
        }

        long currentHeadcount = courseJpaRepository.countByIdLectureId(lecture.getId());

        // 정원 초과 금지
        if (currentHeadcount >= lecture.getCapacity()) {
            throw new BusinessException(CourseExceptionEnum.CAPACITY_FULL);
        }

        Course course = Course.from(courseId);
        courseJpaRepository.save(course);

        return CourseResponse.from(course);
    }
}