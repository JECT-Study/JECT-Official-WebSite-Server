package org.ject.support.common.security;

import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Permission;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.RolePermissions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    private final String TEST_EMAIL = "test@example.com";
    private final Long TEST_APPLICANT_ID = 1L;

    @Test
    void Member_객체로_CustomUserDetails_생성_시_권한에_ROLE_접두사가_추가되는지_확인() {
        // given
        Applicant applicant = Applicant.builder()
                .id(TEST_APPLICANT_ID)
                .email(TEST_EMAIL)
                .name("Test User")
                .phoneNumber("01012345678")
                .status(MemberStatus.ACTIVE)
                .role(Role.APPLY)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(applicant);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorityNames).containsExactly("ROLE_APPLY");
    }

    @Test
    void 파라미터로_CustomUserDetails_생성_시_권한에_ROLE_접두사가_추가되는지_확인() {
        // given
        Role role = Role.SEMESTER;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_APPLICANT_ID, role);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorityNames).containsExactly("ROLE_SEMESTER");
    }

    @Test
    void ADMIN_역할로_CustomUserDetails_생성_시_모든_Permission_이_authority_에_포함되는지_확인() {
        // given
        Role role = Role.ADMIN;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_APPLICANT_ID, role);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities).isNotEmpty();
        assertThat(authorityNames.get(0)).isEqualTo("ROLE_ADMIN");
        assertThat(authorityNames).containsAll(
                RolePermissions.getPermissions(Role.ADMIN).stream()
                        .map(Permission::name)
                        .toList()
        );
    }

    @Test
    void SUPPORTER_역할로_CustomUserDetails_생성_시_role_과_permission_authority_가_함께_반환되는지_확인() {
        // given
        Role role = Role.SUPPORTER;

        // when
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, TEST_APPLICANT_ID, role);
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
