package org.hyejoon.cuvcourse.domain.lecture.cache;

import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureExceptionEnum implements BusinessExceptionEnum {

    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
