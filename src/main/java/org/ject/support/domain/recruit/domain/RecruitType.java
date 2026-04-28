package org.ject.support.domain.recruit.domain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitType {
    SEMESTER("정규 기수 모집"),
    MAKERS("메이커스 모집"),
    SUPPORTERS("운영 서포터즈 모집"),

    // Legacy values remain until existing recruit_type data migrates to SEMESTER + recruitTypeDetail.
    REGULAR("정규 모집"),
    REGULAR_WAITLIST("정규 모집 - 추가합격"),
    BACKFILL("기존 기수 모집"),
    MANUAL("별도 합류"),
    ;

    private final String description;
}
