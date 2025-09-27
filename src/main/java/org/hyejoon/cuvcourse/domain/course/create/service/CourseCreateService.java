package org.hyejoon.cuvcourse.domain.course.create.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.global.lock.DistributedLockDomain;
import org.hyejoon.cuvcourse.global.lock.DistributedLockManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseCreateService {

    private final DistributedLockManager distributedLockManager;
    private final CourseCreateProvider courseCreateProvider;

    public CourseResponse createCourseWithDistributedLock(Long studentId, Long lectureId) {
        try {
            return distributedLockManager.executeWithLock(
                DistributedLockDomain.LECTURE,
                lectureId,
                () -> courseCreateProvider.createCourse(studentId, lectureId)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생: " + lectureId, e);
        }
    }
}
