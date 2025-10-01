package org.hyejoon.cuvcourse.domain.lecture.lecturesearch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LectureSearchRequest(@NotNull String title, @Min(0) int page, @Min(5) int size) {

}
