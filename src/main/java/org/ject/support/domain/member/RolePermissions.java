package org.ject.support.domain.member;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RolePermissions {

    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = createRolePermissions();

    private RolePermissions() {
    }

    public static Set<Permission> getPermissions(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class));
    }

    public static Set<Role> getDefinedRoles() {
        return EnumSet.copyOf(ROLE_PERMISSIONS.keySet());
    }

    private static Map<Role, Set<Permission>> createRolePermissions() {
        Map<Role, Set<Permission>> rolePermissions = new EnumMap<>(Role.class);

        rolePermissions.put(Role.ADMIN, EnumSet.allOf(Permission.class));
        rolePermissions.put(Role.OPERATIONS, EnumSet.of(
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
        ));
        rolePermissions.put(Role.SUPPORTER, EnumSet.of(
                Permission.APPLY_READ,
                Permission.MEMBER_READ,
                Permission.MEMBER_UPDATE,
                Permission.MAIL_TEMPLATE_CREATE,
                Permission.MAIL_TEMPLATE_READ,
                Permission.MAIL_TEMPLATE_UPDATE
        ));
        rolePermissions.put(Role.SEMESTER, EnumSet.noneOf(Permission.class));
        rolePermissions.put(Role.APPLY, EnumSet.noneOf(Permission.class));
        rolePermissions.put(Role.VERIFICATION, EnumSet.noneOf(Permission.class));

        return rolePermissions;
    }
}
