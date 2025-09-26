package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseRegistService extends AbstractCourseRegistService {

    public CourseRegistService(CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        @Qualifier("noLock") DistributedLock noLock) {
        super(validator, courseCreationService, noLock);
    }

    @Override
    @Transactional
    public CourseResponse registerCourse(long studentId, long lectureId) {
        return super.registerCourse(studentId, lectureId);
    }
}
