package org.ject.support.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class CustomUserDetailsTest {

    private final String TEST_EMAIL = "test@example.com";
    private final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("Member 객체로 CustomUserDetails 생성 시 권한에 ROLE_ 접두사가 추가되는지 확인")
    void getAuthorities_FromMember_ShouldAddRolePrefix() {
        // given
        Member member = Member.builder()
                .id(TEST_MEMBER_ID)
                .email(TEST_EMAIL)
                .name("Test User")
                .phoneNumber("01012345678")
                .status(MemberStatus.ACTIVE)
                .role(Role.TEMP)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_TEMP");
    }

    @Test
    @DisplayName("파라미터로 CustomUserDetails 생성 시 권한에 ROLE_ 접두사가 추가되는지 확인")
    void getAuthorities_FromParameters_ShouldAddRolePrefix() {
        // given
        Role role = Role.USER;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_MEMBER_ID, role);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("ADMIN 역할로 CustomUserDetails 생성 시 권한에 ROLE_ 접두사가 추가되는지 확인")
    void getAuthorities_AdminRole_ShouldAddRolePrefix() {
        // given
        Role role = Role.ADMIN;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_MEMBER_ID, role);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }
}
