package org.hyejoon.cuvcourse.domain.student.login.exception;

import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginExceptionEnum implements BusinessExceptionEnum {
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 혹은 패스워드가 일치하지 않습니다."),;

    private final HttpStatus status;
    private final String message;
}
