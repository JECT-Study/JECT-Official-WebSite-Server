package org.ject.support.admin.component;

import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
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
    ApplicantRepository applicantRepository;

    @Test
    void 존재하지않는_이메일로_관리자계정의_정보를_조회할_경우_NOT_FOUND_ADMIN_예외가_발생() {
        // given
        String notFoundEmail = "not_found_admin@test.com";

        given(applicantRepository.findByEmailAndRoleIn(notFoundEmail, BACKOFFICE_ROLES)).willReturn(Optional.empty());

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
        Applicant foundApplicant = Applicant.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(applicantRepository.findByEmailAndRoleIn(email, BACKOFFICE_ROLES)).willReturn(Optional.of(foundApplicant));

        // when
        Applicant result = adminMemberComponent.getRequiredBackofficeMemberByEmail(email);

        // then
        verify(applicantRepository).findByEmailAndRoleIn(email, BACKOFFICE_ROLES);
        assertEquals(email, result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());
    }

    @Test
    void 이메일로_관리자계정의_정보를_Optional로_조회할_경우_존재하면_Member를_반환() {
        // given
        String email = "admin@test.com";
        Applicant foundApplicant = Applicant.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.SUPPORTER)
                .build();

        given(applicantRepository.findByEmailAndRoleIn(email, BACKOFFICE_ROLES)).willReturn(Optional.of(foundApplicant));

        // when
        Optional<Applicant> result = adminMemberComponent.findBackofficeMemberByEmail(email);

        // then
        verify(applicantRepository).findByEmailAndRoleIn(email, BACKOFFICE_ROLES);
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getRole()).isEqualTo(Role.SUPPORTER);
    }

    @Test
    void 이메일로_관리자계정의_정보를_Optional로_조회할_경우_없으면_빈_Optional을_반환() {
        // given
        String email = "not_found_admin@test.com";

        given(applicantRepository.findByEmailAndRoleIn(email, BACKOFFICE_ROLES)).willReturn(Optional.empty());

        // when
        Optional<Applicant> result = adminMemberComponent.findBackofficeMemberByEmail(email);

        // then
        verify(applicantRepository).findByEmailAndRoleIn(email, BACKOFFICE_ROLES);
        assertThat(result).isEmpty();
    }

    @Test
    void id로_관리자계정의_정보를_조회할_경우_Member_엔티티를_반환() {
        // given
        Long memberId = 1L;
        Applicant foundApplicant = Applicant.builder()
                .id(memberId)
                .email("admin@test.com")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(applicantRepository.findByIdAndRoleIn(memberId, BACKOFFICE_ROLES)).willReturn(Optional.of(foundApplicant));

        // when
        Applicant result = adminMemberComponent.getRequiredBackofficeMemberById(memberId);

        // then
        verify(applicantRepository).findByIdAndRoleIn(memberId, BACKOFFICE_ROLES);
        assertEquals(memberId, result.getId());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());
    }

    @Test
    void 존재하지않는_id로_관리자계정의_정보를_조회할_경우_NOT_FOUND_ADMIN_예외가_발생() {
        // given
        Long memberId = 999L;

        given(applicantRepository.findByIdAndRoleIn(memberId, BACKOFFICE_ROLES)).willReturn(Optional.empty());

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
        Applicant applicant = Applicant.builder()
                .id(1L)
                .email("test@test.com")
                .status(MemberStatus.ACTIVE)
                .role(adminRole)
                .build();
        MemberStatus changeStatus = MemberStatus.LOCKED;

        // when
        adminMemberComponent.changeMemberStatus(applicant, changeStatus);

        // then
        verify(applicantRepository).save(applicant);
        assertEquals(changeStatus, applicant.getStatus());
    }

    @Test
    void 회원의_상태가_이미_같으면_저장하지_않는다() {
        // given
        Applicant applicant = Applicant.builder()
                .id(1L)
                .email("test@test.com")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        // when
        adminMemberComponent.changeMemberStatus(applicant, MemberStatus.ACTIVE);

        // then
        verify(applicantRepository, never()).save(applicant);
        assertEquals(MemberStatus.ACTIVE, applicant.getStatus());
    }
}
