package org.ject.support.domain.member;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    ADMIN,        // 관리자
    OPERATIONS,   // 운영팀
    SUPPORTER,    // 서포터즈
    SEMESTER,     // 동아리 원
    APPLY,        // 지원자
    VERIFICATION; // 임시

    public boolean isBackoffice() {
        return this == ADMIN || this == OPERATIONS || this == SUPPORTER;
    }

    public static Set<Role> backofficeRoles() {
        return Arrays.stream(values())
                .filter(Role::isBackoffice)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    }
}
