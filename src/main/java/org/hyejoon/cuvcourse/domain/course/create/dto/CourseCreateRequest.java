package org.hyejoon.cuvcourse.domain.course.create.dto;

import jakarta.validation.constraints.NotNull;

public record CourseCreateRequest(
    @NotNull(message = "강의 ID는 필수입니다.") Long lectureId
) {

}
