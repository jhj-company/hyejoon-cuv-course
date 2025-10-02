package org.hyejoon.cuvcourse.domain.course.courseregist.dto;

import jakarta.validation.constraints.NotNull;

public record CourseRegistRequest(@NotNull(message = "강의 ID는 필수입니다.") Long lectureId) {

}
