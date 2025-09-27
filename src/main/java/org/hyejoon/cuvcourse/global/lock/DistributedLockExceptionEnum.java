package org.hyejoon.cuvcourse.global.lock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.global.exception.BusinessExceptionEnum;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DistributedLockExceptionEnum implements BusinessExceptionEnum {

    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "락 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}
