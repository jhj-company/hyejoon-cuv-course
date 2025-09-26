package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CourseRegistSpinLockService extends AbstractCourseRegistService {

    public CourseRegistSpinLockService(CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        @Qualifier("spinLock") DistributedLock distributedLock) {
        super(validator, courseCreationService, distributedLock);
    }
}
