package org.hyejoon.cuvcourse.domain.course.create.service;

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
import org.hyejoon.cuvcourse.global.redis.service.LockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCreateService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final LockService lockService;

    @Transactional
    public CourseResponse createCourse(Long studentId, Long lectureId) {

        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(CourseExceptionEnum.STUDENT_NOT_FOUND));
        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(CourseExceptionEnum.LECTURE_NOT_FOUND));

        CourseId courseId = CourseId.of(lecture, student);
        String lectureKey = "lecture:" + lectureId;

        lockService.acquireLockWithRetry(lectureKey);
        try {
            long currentHeadcount = courseJpaRepository.countByIdLecture(lecture);
            if (currentHeadcount >= lecture.getCapacity()) {
                throw new BusinessException(CourseExceptionEnum.CAPACITY_FULL);
            }

            if (courseJpaRepository.existsById(courseId)) {
                throw new BusinessException(CourseExceptionEnum.ALREADY_REGISTERED);
            }

            boolean canRegister = lockService.tryAcquire(lectureKey, lecture.getCapacity());
            if (!canRegister) {
                throw new BusinessException(CourseExceptionEnum.CAPACITY_FULL);
            }
            Course course = Course.from(courseId);
            courseJpaRepository.save(course);

            return CourseResponse.from(course);

        } catch (Exception e) {
            lockService.decrementCount(lectureKey);
            throw e;
        } finally {
            lockService.releaseLock(lectureKey);
        }
    }
}
