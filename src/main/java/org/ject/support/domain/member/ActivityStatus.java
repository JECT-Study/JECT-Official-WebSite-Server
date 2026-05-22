package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityStatus {
    ACTIVE("활동중"),
    COMPLETED("완주"),
    ENDED("활동종료"),
    DROPOUT("중도하차");

    private final String description;
}
