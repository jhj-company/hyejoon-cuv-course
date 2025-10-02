package org.hyejoon.cuvcourse.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(BusinessExceptionEnum exEnum) {
        super(exEnum.getMessage());
        this.status = exEnum.getStatus();
    }
}
