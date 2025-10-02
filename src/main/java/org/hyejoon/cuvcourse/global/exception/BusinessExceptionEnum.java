package org.hyejoon.cuvcourse.global.exception;

import org.springframework.http.HttpStatus;

public interface BusinessExceptionEnum {
    HttpStatus getStatus();

    String getMessage();
}
