package org.hyejoon.cuvcourse.domain.course.cache;

/**
 * Redis 접근 실패를 상위 계층으로 전달하기 위한 런타임 예외.
 */
public class CourseCapacityCacheException extends RuntimeException {

    public CourseCapacityCacheException(String message) {
        super(message);
    }

    public CourseCapacityCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
