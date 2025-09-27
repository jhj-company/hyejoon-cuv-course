package org.hyejoon.cuvcourse.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
            .map(ObjectError::getDefaultMessage).collect(Collectors.joining(", "));
        GlobalResponse<Void> response = GlobalResponse.badRequest(message, null);
        return ResponseEntity.status(response.status()).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalResponse<Void>> handleConstraintViolation(
        ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .map(violation -> violation.getMessage()).collect(Collectors.joining(", "));
        GlobalResponse<Void> response = GlobalResponse.badRequest(message, null);
        return ResponseEntity.status(response.status()).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<GlobalResponse<Void>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
            .map(ObjectError::getDefaultMessage).collect(Collectors.joining(", "));
        GlobalResponse<Void> response = GlobalResponse.badRequest(message, null);
        return ResponseEntity.status(response.status()).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalResponse<Void>> handleBusinessException(BusinessException ex) {
        GlobalResponse<Void> response = new GlobalResponse<>(ex.getStatus(), ex.getMessage(), null);
        return ResponseEntity.status(response.status()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Void>> handleException(Exception ex) {
        log.warn("Exception occured: {}", ex.getMessage());
        GlobalResponse<Void> response = new GlobalResponse<>(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error", null);
        return ResponseEntity.status(response.status()).body(response);
    }
}
