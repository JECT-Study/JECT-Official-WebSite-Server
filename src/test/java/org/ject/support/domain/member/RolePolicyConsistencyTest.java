package org.ject.support.domain.member;

import org.ject.support.common.security.config.RoleHierarchySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RolePolicyConsistencyTest {

    @Test
    void 모든_Role_은_권한_정책과_계층_정책에_누락_없이_반영되어야_한다() {
        Set<Role> allRoles = EnumSet.copyOf(Arrays.asList(Role.values()));

        assertThat(RolePermissions.getDefinedRoles()).isEqualTo(allRoles);
        assertThat(RoleHierarchySpec.getDefinedRoles()).isEqualTo(allRoles);
    }

    @Test
    void RolePermissions_에서_반환한_권한_목록은_외부에서_수정할_수_없다() {
        assertThatThrownBy(() -> RolePermissions.getPermissions(Role.ADMIN).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
