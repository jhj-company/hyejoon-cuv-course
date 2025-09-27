package org.hyejoon.cuvcourse.domain.course.courseregist.service.lock.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.service.AbstractCourseRegistService;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseCreationService;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistrationValidator;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.lock.DistributedLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class CourseRegistRedissonService extends AbstractCourseRegistService {

    public CourseRegistRedissonService(CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        @Qualifier("redissonLock") DistributedLock distributedLock) {
        super(validator, courseCreationService, distributedLock);
    }
}
