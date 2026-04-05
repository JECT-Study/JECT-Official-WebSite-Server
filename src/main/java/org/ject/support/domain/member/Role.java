package org.ject.support.domain.member;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    ADMIN(true),         // 관리자
    OPERATIONS(true),    // 운영팀
    SUPPORTER(true),     // 서포터즈
    SEMESTER(false),     // 동아리 원
    APPLY(false),        // 지원자
    VERIFICATION(false); // 임시

    private final boolean backoffice;

    Role(boolean backoffice) {
        this.backoffice = backoffice;
    }

    public boolean isBackoffice() {
        return backoffice;
    }

    public static Set<Role> backofficeRoles() {
        return Arrays.stream(values())
                .filter(Role::isBackoffice)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    }
}
