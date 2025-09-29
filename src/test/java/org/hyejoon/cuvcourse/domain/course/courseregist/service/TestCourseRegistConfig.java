package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestCourseRegistConfig {

    @Bean
    @Qualifier("courseRegistRedissonService")
    public CourseRegistService redissonCourseRegistService(
        CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("redissonLock") DistributedLock redissonLock) {
        return new CourseRegistService(validator, courseCreationService, lockManager, redissonLock);
    }

    @Bean
    @Qualifier("courseRegistPubSubLockService")
    public CourseRegistService pubSubCourseRegistService(
        CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("pubSubLock") DistributedLock pubSubLock) {
        return new CourseRegistService(validator, courseCreationService, lockManager, pubSubLock);
    }

    @Bean
    @Qualifier("courseRegistSpinLockService")
    public CourseRegistService spinCourseRegistService(
        CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("spinLock") DistributedLock spinLock) {
        return new CourseRegistService(validator, courseCreationService, lockManager, spinLock);
    }

    @Bean
    @Qualifier("courseRegistNoLockService")
    public CourseRegistService noLockCourseRegistService(
        CourseRegistrationValidator validator,
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("noLock") DistributedLock noLock) {
        return new CourseRegistService(validator, courseCreationService, lockManager, noLock);
    }
}
