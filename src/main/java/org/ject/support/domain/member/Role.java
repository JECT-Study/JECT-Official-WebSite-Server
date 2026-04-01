package org.ject.support.domain.member;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum Role {
    ADMIN(adminPermissions()),           // 관리자
    OPERATIONS(operationsPermissions()), // 운영팀
    SUPPORTER(supporterPermissions()),   // 서포터즈
    SEMESTER(noPermissions()),           // 동아리 원
    APPLY(noPermissions()),              // 지원자
    VERIFICATION(noPermissions());       // 임시

    private final Set<Permission> permissions;
    public static final Set<Role> BACKOFFICE_ROLES = EnumSet.of(ADMIN, SUPPORTER, OPERATIONS);

    Role(final Set<Permission> permissions) {
        this.permissions = permissions;
    }

    private static Set<Permission> adminPermissions() {
        return EnumSet.allOf(Permission.class);
    }

    private static Set<Permission> operationsPermissions() {
        return EnumSet.of(
                Permission.APPLY_CREATE,
                Permission.APPLY_READ,
                Permission.APPLY_UPDATE,
                Permission.APPLY_DELETE,
                Permission.APPLY_RESULT_FULL,
                Permission.MEMBER_CREATE,
                Permission.MEMBER_READ,
                Permission.MEMBER_UPDATE,
                Permission.MEMBER_DELETE,
                Permission.MAIL_TEMPLATE_CREATE,
                Permission.MAIL_TEMPLATE_READ,
                Permission.MAIL_TEMPLATE_UPDATE,
                Permission.MAIL_TEMPLATE_DELETE,
                Permission.MAIL_SEND_FULL
        );
    }

    private static Set<Permission> supporterPermissions() {
        return EnumSet.of(
                Permission.APPLY_READ,
                Permission.MEMBER_READ,
                Permission.MEMBER_UPDATE,
                Permission.MAIL_TEMPLATE_CREATE,
                Permission.MAIL_TEMPLATE_READ,
                Permission.MAIL_TEMPLATE_UPDATE
        );
    }

    private static Set<Permission> noPermissions() {
        return EnumSet.noneOf(Permission.class);
    }
}
