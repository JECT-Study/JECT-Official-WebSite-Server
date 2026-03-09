package org.ject.support.domain.recruit.domain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitType {
    REGULAR("정규 모집"),
    REGULAR_WAITLIST("정규 모집 - 추가합격"),
    BACKFILL("기존 기수 모집"),
    MANUAL("별도 합류"),
    ;

    private final String description;
}
