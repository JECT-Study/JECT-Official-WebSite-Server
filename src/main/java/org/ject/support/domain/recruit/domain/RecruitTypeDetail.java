package org.ject.support.domain.recruit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitTypeDetail {
    REGULAR("정규 모집"),
    NEW("신규 모집"),
    REFILL("충원 모집"),
    ;

    private final String description;
}
