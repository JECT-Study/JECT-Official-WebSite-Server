package org.ject.support.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Availability {
    UNAVAILABLE("불가능"),
    CONSIDER_LATER("추후 고려"),
    AVAILABLE_BY_TOPIC("주제에 따라 가능"),
    HIGHLY_AVAILABLE("적극 가능");

    private final String description;
}
