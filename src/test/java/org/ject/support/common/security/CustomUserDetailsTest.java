package org.ject.support.common.security;

import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Permission;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
                .role(Role.APPLY)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorityNames).containsExactly("ROLE_APPLY");
    }

    @Test
    @DisplayName("파라미터로 CustomUserDetails 생성 시 권한에 ROLE_ 접두사가 추가되는지 확인")
    void getAuthorities_FromParameters_ShouldAddRolePrefix() {
        // given
        Role role = Role.SEMESTER;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_MEMBER_ID, role);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorityNames).containsExactly("ROLE_SEMESTER");
    }

    @Test
    @DisplayName("ADMIN 역할로 CustomUserDetails 생성 시 모든 Permission 이 authority 에 포함되는지 확인")
    void getAuthorities_AdminRole_ShouldContainAllPermissions() {
        // given
        Role role = Role.ADMIN;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_MEMBER_ID, role);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorityNames.get(0)).isEqualTo("ROLE_ADMIN");
        assertThat(authorityNames).containsAll(
                Role.ADMIN.getPermissions().stream()
                        .map(Permission::name)
                        .toList()
        );
    }

    @Test
    @DisplayName("SUPPORTER 역할로 CustomUserDetails 생성 시 role 과 permission authority 가 함께 반환되는지 확인")
    void getAuthorities_SupporterRole_ShouldContainRoleAndPermissions() {
        // given
        Role role = Role.SUPPORTER;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_MEMBER_ID, role);
        List<String> authorityNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorityNames.get(0)).isEqualTo("ROLE_SUPPORTER");
        assertThat(authorityNames).contains(
                Permission.APPLY_READ.name(),
                Permission.MEMBER_READ.name(),
                Permission.MEMBER_UPDATE.name(),
                Permission.MAIL_TEMPLATE_CREATE.name(),
                Permission.MAIL_TEMPLATE_READ.name(),
                Permission.MAIL_TEMPLATE_UPDATE.name()
        );
    }
}
