package org.hyejoon.cuvcourse.domain.course.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseLockPrefixEnum {
    COURSE_LOCK("course-service:course-lock:");

    private final String prefix;
}
