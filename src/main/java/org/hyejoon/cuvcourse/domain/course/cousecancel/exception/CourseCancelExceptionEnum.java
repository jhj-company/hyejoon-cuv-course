package org.hyejoon.cuvcourse.domain.course.cousecancel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseCancelExceptionEnum implements BusinessExceptionEnum {
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 신청 내역이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
