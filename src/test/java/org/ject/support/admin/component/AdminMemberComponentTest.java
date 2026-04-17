package org.ject.support.admin.component;

import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminMemberComponentTest extends UnitTestSupport {

    private static final Set<Role> BACKOFFICE_ROLES = Role.backofficeRoles();

    @InjectMocks
    AdminMemberComponent adminMemberComponent;

    @Mock
    MemberRepository memberRepository;

    @Test
    void 존재하지않는_이메일로_관리자계정의_정보를_조회할_경우_NOT_FOUND_ADMIN_예외가_발생() {
        // given
        String notFoundEmail = "not_found_admin@test.com";

        given(memberRepository.findByEmailAndRoleIn(notFoundEmail, BACKOFFICE_ROLES)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminMemberComponent.getRequiredBackofficeMemberByEmail(notFoundEmail))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);
    }

    @Test
    void 이메일로_관리자계정의_정보를_조회할_경우_Member_엔티티를_반환() {
        // given
        String email = "admin@test.com";
        Member foundMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByEmailAndRoleIn(email, BACKOFFICE_ROLES)).willReturn(Optional.of(foundMember));

        // when
        Member result = adminMemberComponent.getRequiredBackofficeMemberByEmail(email);

        // then
        verify(memberRepository).findByEmailAndRoleIn(email, BACKOFFICE_ROLES);
        assertEquals(email, result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());
    }

    @Test
    void 이메일로_관리자계정의_정보를_Optional로_조회할_경우_존재하면_Member를_반환() {
        // given
        String email = "admin@test.com";
        Member foundMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.SUPPORTER)
                .build();

        given(memberRepository.findByEmailAndRoleIn(email, BACKOFFICE_ROLES)).willReturn(Optional.of(foundMember));

        // when
        Optional<Member> result = adminMemberComponent.findBackofficeMemberByEmail(email);

        // then
        verify(memberRepository).findByEmailAndRoleIn(email, BACKOFFICE_ROLES);
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getRole()).isEqualTo(Role.SUPPORTER);
    }

    @Test
    void 이메일로_관리자계정의_정보를_Optional로_조회할_경우_없으면_빈_Optional을_반환() {
        // given
        String email = "not_found_admin@test.com";

        given(memberRepository.findByEmailAndRoleIn(email, BACKOFFICE_ROLES)).willReturn(Optional.empty());

        // when
        Optional<Member> result = adminMemberComponent.findBackofficeMemberByEmail(email);

        // then
        verify(memberRepository).findByEmailAndRoleIn(email, BACKOFFICE_ROLES);
        assertThat(result).isEmpty();
    }

    @Test
    void id로_관리자계정의_정보를_조회할_경우_Member_엔티티를_반환() {
        // given
        Long memberId = 1L;
        Member foundMember = Member.builder()
                .id(memberId)
                .email("admin@test.com")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByIdAndRoleIn(memberId, BACKOFFICE_ROLES)).willReturn(Optional.of(foundMember));

        // when
        Member result = adminMemberComponent.getRequiredBackofficeMemberById(memberId);

        // then
        verify(memberRepository).findByIdAndRoleIn(memberId, BACKOFFICE_ROLES);
        assertEquals(memberId, result.getId());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());
    }

    @Test
    void 존재하지않는_id로_관리자계정의_정보를_조회할_경우_NOT_FOUND_ADMIN_예외가_발생() {
        // given
        Long memberId = 999L;

        given(memberRepository.findByIdAndRoleIn(memberId, BACKOFFICE_ROLES)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminMemberComponent.getRequiredBackofficeMemberById(memberId))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);
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

    @Test
    void 회원의_상태가_이미_같으면_저장하지_않는다() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        // when
        adminMemberComponent.changeMemberStatus(member, MemberStatus.ACTIVE);

        // then
        verify(memberRepository, never()).save(member);
        assertEquals(MemberStatus.ACTIVE, member.getStatus());
    }
}
