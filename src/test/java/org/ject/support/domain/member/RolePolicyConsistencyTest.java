package org.ject.support.domain.member;

import org.ject.support.common.security.config.RoleHierarchySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RolePolicyConsistencyTest {

    @Test
    void 모든_Role_은_권한_정책과_계층_정책에_누락_없이_반영되어야_한다() {
        Set<Role> allRoles = EnumSet.copyOf(Arrays.asList(Role.values()));

        assertThat(RolePermissions.getDefinedRoles()).isEqualTo(allRoles);
        assertThat(RoleHierarchySpec.getDefinedRoles()).isEqualTo(allRoles);
    }
}
