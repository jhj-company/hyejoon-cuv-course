package org.hyejoon.cuvcourse.domain.student.login.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudentExceptionEnum implements BusinessExceptionEnum {
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
