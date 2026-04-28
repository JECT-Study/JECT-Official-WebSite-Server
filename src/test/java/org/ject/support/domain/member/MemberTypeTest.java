package org.ject.support.domain.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTypeTest {

    @Test
    void 구성원_타입_값을_제공한다() {
        assertThat(MemberType.values())
                .contains(
                        MemberType.SEMESTER,
                        MemberType.MAKERS,
                        MemberType.SUPPORTERS
                );
    }

    @Test
    void 서포터즈_권한은_운영_서포터즈_구성원_타입으로_변환한다() {
        assertThat(MemberType.fromRole(Role.SUPPORTER)).isEqualTo(MemberType.SUPPORTERS);
    }

    @Test
    void 서포터즈_외_권한은_기본_구성원_타입으로_변환한다() {
        assertThat(MemberType.fromRole(Role.SEMESTER)).isEqualTo(MemberType.SEMESTER);
        assertThat(MemberType.fromRole(Role.ADMIN)).isEqualTo(MemberType.SEMESTER);
    }
}
