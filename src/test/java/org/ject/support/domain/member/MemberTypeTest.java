package org.ject.support.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTypeTest {

    @Test
    @DisplayName("구성원 타입 값을 제공한다")
    void provide_member_type_values() {
        assertThat(MemberType.values())
                .contains(
                        MemberType.SEMESTER,
                        MemberType.MAKERS,
                        MemberType.SUPPORTERS
                );
    }
}
