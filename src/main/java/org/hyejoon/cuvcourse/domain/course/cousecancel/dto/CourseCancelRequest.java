package org.hyejoon.cuvcourse.domain.course.cousecancel.dto;

import jakarta.validation.constraints.NotNull;

public record CourseCancelRequest(@NotNull Long lectureId) {

}
