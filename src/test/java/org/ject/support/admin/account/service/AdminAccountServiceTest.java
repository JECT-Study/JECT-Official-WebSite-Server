package org.ject.support.admin.account.service;

import org.ject.support.admin.account.dto.AdminAccountActiveUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.dto.AdminAccountRoleUpdateRequest;
import org.ject.support.admin.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminAccountServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminAccountService adminAccountService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AdminMemberComponent adminMemberComponent;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "admin@ject.kr";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Test
    void 관리자_계정_생성_성공() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "김젝트", Role.OPERATIONS);

        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        adminAccountService.createAccount(request);

        // then
        var memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedMember.getPin()).isEqualTo(ENCODED_PASSWORD);
        assertThat(savedMember.getName()).isEqualTo("김젝트");
        assertThat(savedMember.getRole()).isEqualTo(Role.OPERATIONS);
        assertThat(savedMember.getSemesterId()).isEqualTo(1L);
    }

    @Test
    void 관리자_계정_생성_시_빈_이름은_null로_저장한다() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "", Role.ADMIN);

        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        adminAccountService.createAccount(request);

        // then
        var memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        assertThat(memberCaptor.getValue().getName()).isNull();
    }

    @Test
    void 관리자_계정_생성_시_공백_이름은_null로_저장한다() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "   ", Role.ADMIN);

        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        adminAccountService.createAccount(request);

        // then
        var memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        assertThat(memberCaptor.getValue().getName()).isNull();
    }

    @Test
    void 관리자_계정_생성_실패_이미_사용중인_이메일() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "김젝트", Role.ADMIN);

        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // expected
        assertThatThrownBy(() -> adminAccountService.createAccount(request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.DUPLICATE_ADMIN_EMAIL);

        verify(memberRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(org.mockito.ArgumentMatchers.any(Member.class));
    }

    @Test
    void 관리자_계정_생성_실패_관리자_계정_유형이_아닌_role() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "김젝트", Role.SEMESTER);

        // expected
        assertThatThrownBy(() -> adminAccountService.createAccount(request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);

        verify(memberRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(org.mockito.ArgumentMatchers.any(Member.class));
    }

    @Test
    void 관리자_계정_권한_수정_성공() {
        // given
        var memberId = 1L;
        var request = new AdminAccountRoleUpdateRequest(Role.SUPPORTER);
        var member = Member.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .name("김젝트")
                .phoneNumber("01012345678")
                .role(Role.OPERATIONS)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(member);

        // when
        adminAccountService.updateRole(memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        assertThat(member.getRole()).isEqualTo(Role.SUPPORTER);
        assertThat(member.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(member.getName()).isEqualTo("김젝트");
        assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(member.getSemesterId()).isEqualTo(1L);
    }

    @Test
    void 관리자_계정_권한_수정_실패_관리자_계정_유형이_아닌_role() {
        // given
        var request = new AdminAccountRoleUpdateRequest(Role.SEMESTER);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateRole(1L, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void 관리자_계정_권한_수정_실패_존재하지_않는_관리자() {
        // given
        var memberId = 999L;
        var request = new AdminAccountRoleUpdateRequest(Role.SUPPORTER);

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId))
                .willThrow(new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateRole(memberId, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);

        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
    }

    @Test
    void 관리자_계정_비활성화_성공() {
        // given
        var memberId = 1L;
        var request = new AdminAccountActiveUpdateRequest(false);
        var member = Member.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .role(Role.OPERATIONS)
                .status(MemberStatus.ACTIVE)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(member);

        // when
        adminAccountService.updateActive(memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        verify(adminMemberComponent).changeMemberStatus(eq(member), eq(MemberStatus.LOCKED));
    }

    @Test
    void 관리자_계정_활성화_성공() {
        // given
        var memberId = 1L;
        var request = new AdminAccountActiveUpdateRequest(true);
        var member = Member.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .role(Role.OPERATIONS)
                .status(MemberStatus.LOCKED)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(member);

        // when
        adminAccountService.updateActive(memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        verify(adminMemberComponent).changeMemberStatus(eq(member), eq(MemberStatus.ACTIVE));
    }

    @Test
    void 관리자_계정_활성화_상태_수정_실패_존재하지_않는_관리자() {
        // given
        var memberId = 999L;
        var request = new AdminAccountActiveUpdateRequest(false);

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId))
                .willThrow(new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(memberId, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);

        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
    }
}
