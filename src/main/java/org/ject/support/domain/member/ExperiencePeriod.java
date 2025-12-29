package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExperiencePeriod {
    NONE("경험 없음"),
    ONE_TO_TWO("1~2년"),
    THREE_TO_FOUR("3~4년"),
    FIVE_PLUS("5년 이상");

    private final String description;
}
