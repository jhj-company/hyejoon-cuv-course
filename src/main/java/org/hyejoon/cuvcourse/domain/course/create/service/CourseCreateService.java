package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.ALREADY_REGISTERED;
import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.CAPACITY_FULL;
import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.LECTURE_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCreateService {

    private final CourseJpaRepository courseJpaRepository;

    @Transactional
    public CourseResponse createCourse(Long studentId, Long lectureId) {

        CourseId courseId = CourseId.of(lectureId, studentId);

        // 2. 수강 신청 중복 확인
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(ALREADY_REGISTERED);
        }

        // 3. 정원 및 현재 인원 조회 및 검증
        List<Object[]> results = courseJpaRepository.findLectureCapacityAndCurrentCount(lectureId);

        if (results.isEmpty()) {
            throw new BusinessException(LECTURE_NOT_FOUND);
        }

        Object[] result = results.get(0);
        int capacity = (Integer) result[0];
        long currentHeadcount = (Long) result[1];

        if (currentHeadcount >= capacity) {
            throw new BusinessException(CAPACITY_FULL);
        }

        Course course = Course.from(courseId);

        Course savedCourse = courseJpaRepository.save(course);

        return CourseResponse.from(savedCourse);
    }
}