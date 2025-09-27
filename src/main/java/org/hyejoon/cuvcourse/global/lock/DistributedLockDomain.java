package org.hyejoon.cuvcourse.global.lock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DistributedLockDomain {
    LECTURE("lecture");

    private final String domain;
}
