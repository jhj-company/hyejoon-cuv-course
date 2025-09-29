package org.hyejoon.cuvcourse.domain.course.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseExceptionEnum implements BusinessExceptionEnum {

    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생을 찾을 수 없습니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강의를 찾을 수 없습니다."),
    CAPACITY_FULL(HttpStatus.CONFLICT, "수강 인원이 모두 찼습니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 수강신청한 강의입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 시도해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}