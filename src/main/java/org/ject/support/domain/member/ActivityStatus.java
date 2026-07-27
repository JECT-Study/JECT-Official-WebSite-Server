package org.ject.support.domain.member;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityStatus {
    ACTIVE("활동 중"),
    COMPLETED("완주"),
    WITHDRAWN("탈퇴"),
    ENDED("활동 종료"),
    DROPOUT("중도 이탈");

    private final String description;

    public boolean isAvailableFor(MemberType type) {
        return switch (type) {
            case SEMESTER -> this == ACTIVE || this == COMPLETED || this == WITHDRAWN;
            case MAKERS -> this == ACTIVE || this == ENDED || this == DROPOUT;
            case SUPPORTERS -> this == ACTIVE || this == ENDED || this == DROPOUT;
        };
    }

    public static boolean isAllAvailableFor(List<ActivityStatus> statuses, MemberType type) {
        if (statuses == null || statuses.isEmpty()) {
            return true;
        }

        return statuses.stream()
            .allMatch(status -> status.isAvailableFor(type));
    }
}
