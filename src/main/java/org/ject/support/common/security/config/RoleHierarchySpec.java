package org.ject.support.common.security.config;

import org.ject.support.domain.member.Role;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleHierarchySpec {

    private static final Map<Role, Set<Role>> HIERARCHY = Map.of(
            Role.ADMIN, EnumSet.of(Role.SEMESTER),
            Role.OPERATIONS, EnumSet.of(Role.SEMESTER),
            Role.SUPPORTER, EnumSet.of(Role.SEMESTER),
            Role.SEMESTER, EnumSet.of(Role.APPLY),
            Role.APPLY, EnumSet.of(Role.VERIFICATION)
    );

    private RoleHierarchySpec() {
    }

    public static String hierarchyExpression() {
        return HIERARCHY.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(child -> "ROLE_" + entry.getKey().name() + " > ROLE_" + child.name()))
                .collect(Collectors.joining("\n"));
    }

    // 권한누락 방지용
    public static Set<Role> getDefinedRoles() {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        HIERARCHY.forEach((parent, children) -> {
            roles.add(parent);
            roles.addAll(children);
        });
        return roles;
    }
}
