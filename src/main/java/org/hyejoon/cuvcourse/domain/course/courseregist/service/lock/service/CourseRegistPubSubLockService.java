package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseCreationService;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistrationValidator;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.lock.DistributedLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CourseRegistPubSubLockService extends AbstractCourseRegistService {

    public CourseRegistPubSubLockService(CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        @Qualifier("pubSubLock") DistributedLock distributedLock) {
        super(validator, courseCreationService, distributedLock);
    }
}
