package org.hyejoon.cuvcourse.global.dto;

import org.springframework.http.HttpStatus;

public record GlobalResponse<T>(HttpStatus status, String message, T data) {
    public static <T> GlobalResponse<T> ok(String message, T data) {
        return new GlobalResponse<T>(HttpStatus.OK, message, data);
    }

    public static <T> GlobalResponse<T> notFound(String message, T data) {
        return new GlobalResponse<T>(HttpStatus.NOT_FOUND, message, data);
    }

    public static <T> GlobalResponse<T> unauthorized(String message, T data) {
        return new GlobalResponse<T>(HttpStatus.UNAUTHORIZED, message, data);
    }

    public static <T> GlobalResponse<T> forbidden(String message, T data) {
        return new GlobalResponse<T>(HttpStatus.FORBIDDEN, message, data);
    }

    public static <T> GlobalResponse<T> badRequest(String message, T data) {
        return new GlobalResponse<T>(HttpStatus.BAD_REQUEST, message, data);
    }
}
