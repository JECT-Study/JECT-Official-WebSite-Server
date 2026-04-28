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

    @Test
    @DisplayName("서포터즈 권한은 운영 서포터즈 구성원 타입으로 변환한다")
    void convert_supporter_role_to_supporters_type() {
        assertThat(MemberType.fromRole(Role.SUPPORTER)).isEqualTo(MemberType.SUPPORTERS);
    }

    @Test
    @DisplayName("서포터즈 외 권한은 기본 구성원 타입으로 변환한다")
    void convert_non_supporter_role_to_semester_type() {
        assertThat(MemberType.fromRole(Role.SEMESTER)).isEqualTo(MemberType.SEMESTER);
        assertThat(MemberType.fromRole(Role.ADMIN)).isEqualTo(MemberType.SEMESTER);
    }
}
