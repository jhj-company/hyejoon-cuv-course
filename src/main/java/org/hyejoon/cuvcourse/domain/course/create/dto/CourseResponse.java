package org.hyejoon.cuvcourse.domain.course.create.dto;

import java.time.LocalDateTime;
import org.hyejoon.cuvcourse.domain.course.entity.Course;

public record CourseResponse(
    Long studentId,
    Long lectureId,
    LocalDateTime createdAt
) {

    // Course 엔티티를 CourseResponse DTO로 변환해주는 정적 메소드
    public static CourseResponse from(Course course) {
        return new CourseResponse(
            course.getId().getStudent().getId(),
            course.getId().getLecture().getId(),
            course.getCreatedAt()
        );
    }
}