package org.ject.support.domain.member;

import org.ject.support.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    void 멤버_엔티티_생성에_성공() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        JobFamily jobFamily = JobFamily.BE;
        Role role = Role.SEMESTER;
        Region region = Region.SEOUL;

        // when
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .role(role)
                .region(region)
                .status(MemberStatus.ACTIVE)
                .build();

        // then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getRegion()).isEqualTo(region);
    }

    @Test
    void 멤버_엔티티_최소_필수_필드로_생성에_성공() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        Role role = Role.SEMESTER;
        Region region = Region.OVERSEAS;

        // when
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .role(role)
                .region(region)
                .status(MemberStatus.ACTIVE)
                .build();

        // then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getRegion()).isEqualTo(region);

    }

    @Test
    void 멤버의_이름이_없을_경우_프로필_완성_확인에_FALSE_반환() {
        // given
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .email("noName@ject.com")
                .build();

        // when
        boolean result = member.isProfileComplete();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 멤버의_이름과_핸드폰번호가_있을_경우_프로필_완성_확인에_TRUE_반환() {
        // given
        Member member = Member.builder()
                .name("이름")
                .phoneNumber("01012345678")
                .build();

        // when
        boolean result = member.isProfileComplete();

        // then
        assertThat(result).isTrue();
    }
}
