package org.hyejoon.cuvcourse.domain.course.courseregist.dto;

import java.time.LocalDateTime;
import org.hyejoon.cuvcourse.domain.course.entity.Course;

public record CourseResponse(Long studentId, Long lectureId, LocalDateTime createdAt
) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(
            course.getId().getStudent().getId(),
            course.getId().getLecture().getId(),
            course.getCreatedAt()
        );
    }
}