package org.hyejoon.cuvcourse.global.exception;

import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

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
        GlobalResponse<Void> response = GlobalResponse.badRequest("Internal server error", null);
        return ResponseEntity.status(response.status()).body(response);
    }
}
