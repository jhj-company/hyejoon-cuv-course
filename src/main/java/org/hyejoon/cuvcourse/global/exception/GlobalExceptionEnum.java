package org.hyejoon.cuvcourse.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalExceptionEnum implements BusinessExceptionEnum {

    LOCK_ACQUIRE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "락 흭득 실패.");

    private final HttpStatus status;

    private final String message;
}
