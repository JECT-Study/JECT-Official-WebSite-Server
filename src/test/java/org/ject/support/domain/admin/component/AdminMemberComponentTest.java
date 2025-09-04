package org.ject.support.domain.admin.component;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class AdminMemberComponentTest extends UnitTestSupport {

    @InjectMocks
    AdminMemberComponent adminMemberComponent;

    @Mock
    MemberRepository memberRepository;

    @Test
    void 존재하지않는_이메일로_관리자계정의_정보를_조회할_경우_NOT_FOUND_ADMIN_예외가_발생() {
        // given
        String notFoundEmail = "not_found_admin@test.com";
        Role adminRole = Role.ADMIN;

        given(memberRepository.findByEmailAndRole(notFoundEmail, adminRole)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminMemberComponent.getMemberAdminByEmail(notFoundEmail))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);
    }

    @Test
    void 이메일로_관리자계정의_정보를_조회할_경우_Member_엔티티를_반환() {
        // given
        String email = "admin@test.com";
        Role adminRole = Role.ADMIN;
        Member foundMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(adminRole)
                .build();

        given(memberRepository.findByEmailAndRole(email, adminRole)).willReturn(Optional.of(foundMember));

        // when
        Member result = adminMemberComponent.getMemberAdminByEmail(email);

        // then
        verify(memberRepository).findByEmailAndRole(email, adminRole);
        assertEquals(email, result.getEmail());
        assertEquals(adminRole, result.getRole());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());
    }

    @Test
    void 회원의_상태를_변경할_경우_변경된_상태로_저장된다() {
        // given
        Role adminRole = Role.ADMIN;
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .status(MemberStatus.ACTIVE)
                .role(adminRole)
                .build();
        MemberStatus changeStatus = MemberStatus.LOCKED;

        // when
        adminMemberComponent.changeMemberStatus(member, changeStatus);

        // then
        verify(memberRepository).save(member);
        assertEquals(changeStatus, member.getStatus());
    }
}
