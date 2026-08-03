package org.ject.support.admin.account.service;

import org.ject.support.admin.account.dto.AdminAccountActiveUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.dto.AdminAccountRoleUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountResponse;
import org.ject.support.admin.account.dto.AdminAccountUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.admin.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.applicant.dto.ApplicantAccountProjection;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminAccountServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminAccountService adminAccountService;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private AdminMemberComponent adminMemberComponent;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "admin@ject.kr";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Test
    void 관리자_계정_목록_조회_성공() {
        // given
        var pageable = PageRequest.of(0, 20);
        var condition = new AdminAccountSearchCondition(
                List.of(Role.ADMIN, Role.SUPPORTER),
                List.of(MemberStatus.ACTIVE));
        var projection = new ApplicantAccountProjection(1L, TEST_EMAIL, "김젝트", Role.ADMIN, MemberStatus.ACTIVE);
        var page = new PageImpl<>(List.of(projection), pageable, 1);

        given(applicantRepository.findAccounts(condition, pageable)).willReturn(page);

        // when
        var result = adminAccountService.findAccounts(condition, pageable);

        // then
        assertThat(result.getContent())
                .containsExactly(new AdminAccountResponse(1L, TEST_EMAIL, "김젝트", Role.ADMIN, MemberStatus.ACTIVE));
        verify(applicantRepository).findAccounts(condition, pageable);
    }

    @Test
    void 관리자_계정_목록_조회_실패_관리자_계정_유형이_아닌_role_필터() {
        // given
        var pageable = PageRequest.of(0, 20);
        var condition = new AdminAccountSearchCondition(
                List.of(Role.ADMIN, Role.SEMESTER),
                List.of(MemberStatus.ACTIVE));

        // when, then
        assertThatThrownBy(() -> adminAccountService.findAccounts(condition, pageable))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);

        verify(applicantRepository, never()).findAccounts(any(), eq(pageable));
    }

    @Test
    void 관리자_계정_생성_성공() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "김젝트", Role.OPERATIONS);

        given(applicantRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        adminAccountService.createAccount(request);

        // then
        var applicantCaptor = ArgumentCaptor.forClass(Applicant.class);
        verify(applicantRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(applicantRepository).save(applicantCaptor.capture());

        Applicant savedApplicant = applicantCaptor.getValue();
        assertThat(savedApplicant.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedApplicant.getPin()).isEqualTo(ENCODED_PASSWORD);
        assertThat(savedApplicant.getName()).isEqualTo("김젝트");
        assertThat(savedApplicant.getRole()).isEqualTo(Role.OPERATIONS);
        assertThat(savedApplicant.getMemberType()).isEqualTo(MemberType.SEMESTER);
        assertThat(savedApplicant.getSemesterId()).isEqualTo(1L);
    }

    @Test
    void 관리자_계정_생성_시_빈_이름은_null로_저장한다() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "", Role.ADMIN);

        given(applicantRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        adminAccountService.createAccount(request);

        // then
        var applicantCaptor = ArgumentCaptor.forClass(Applicant.class);
        verify(applicantRepository).save(applicantCaptor.capture());

        assertThat(applicantCaptor.getValue().getName()).isNull();
    }

    @Test
    void 관리자_계정_생성_시_공백_이름은_null로_저장한다() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "   ", Role.ADMIN);

        given(applicantRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        adminAccountService.createAccount(request);

        // then
        var applicantCaptor = ArgumentCaptor.forClass(Applicant.class);
        verify(applicantRepository).save(applicantCaptor.capture());

        assertThat(applicantCaptor.getValue().getName()).isNull();
    }

    @Test
    void 관리자_계정_생성_실패_이미_사용중인_이메일() {
        // given
        var request = new AdminAccountCreateRequest(TEST_EMAIL, TEST_PASSWORD, "김젝트", Role.ADMIN);

        given(applicantRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // expected
        assertThatThrownBy(() -> adminAccountService.createAccount(request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.DUPLICATE_ADMIN_EMAIL);

        verify(applicantRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(applicantRepository, never()).save(org.mockito.ArgumentMatchers.any(Applicant.class));
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

        verify(applicantRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(applicantRepository, never()).save(org.mockito.ArgumentMatchers.any(Applicant.class));
    }

    @Test
    void 관리자_계정_정보_수정_성공() {
        // given
        var requesterId = 2L;
        var memberId = 1L;
        var request = new AdminAccountUpdateRequest("이젝트", Role.SUPPORTER, false);
        var applicant = Applicant.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .name("김젝트")
                .phoneNumber("01012345678")
                .role(Role.OPERATIONS)
                .status(MemberStatus.ACTIVE)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(applicant);

        // when
        adminAccountService.updateAccount(requesterId, memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        verify(applicantRepository, never()).findByEmail(anyString());
        assertThat(applicant.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(applicant.getName()).isEqualTo("이젝트");
        assertThat(applicant.getRole()).isEqualTo(Role.SUPPORTER);
        assertThat(applicant.getStatus()).isEqualTo(MemberStatus.LOCKED);
        assertThat(applicant.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(applicant.getSemesterId()).isEqualTo(1L);
    }

    @Test
    void 관리자_계정_정보_수정_시_빈_이름은_null로_저장한다() {
        // given
        var requesterId = 2L;
        var memberId = 1L;
        var request = new AdminAccountUpdateRequest("   ", Role.ADMIN, true);
        var applicant = Applicant.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .name("김젝트")
                .role(Role.OPERATIONS)
                .status(MemberStatus.ACTIVE)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(applicant);

        // when
        adminAccountService.updateAccount(requesterId, memberId, request);

        // then
        assertThat(applicant.getName()).isNull();
        assertThat(applicant.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(applicant.getRole()).isEqualTo(Role.ADMIN);
        assertThat(applicant.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void 관리자_계정_정보_수정_실패_관리자_계정_유형이_아닌_role() {
        // given
        var request = new AdminAccountUpdateRequest("김젝트", Role.SEMESTER, true);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateAccount(2L, 1L, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(org.mockito.ArgumentMatchers.anyLong());
        verify(applicantRepository, never()).findByEmail(anyString());
    }

    @Test
    void 관리자_계정_정보_수정_실패_본인_계정_비활성화() {
        // given
        var request = new AdminAccountUpdateRequest("김젝트", Role.ADMIN, false);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateAccount(1L, 1L, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.CANNOT_LOCK_SELF);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(org.mockito.ArgumentMatchers.anyLong());
        verify(applicantRepository, never()).findByEmail(anyString());
    }

    @Test
    void 관리자_계정_정보_수정_실패_본인_관리자_권한_제거() {
        // given
        var request = new AdminAccountUpdateRequest("김젝트", Role.OPERATIONS, true);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateAccount(1L, 1L, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.CANNOT_CHANGE_OWN_ADMIN_ROLE);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(org.mockito.ArgumentMatchers.anyLong());
        verify(applicantRepository, never()).findByEmail(anyString());
    }

    @Test
    void 관리자_계정_권한_수정_성공() {
        // given
        var memberId = 1L;
        var request = new AdminAccountRoleUpdateRequest(Role.SUPPORTER);
        var applicant = Applicant.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .name("김젝트")
                .phoneNumber("01012345678")
                .role(Role.OPERATIONS)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(applicant);

        // when
        adminAccountService.updateRole(memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        assertThat(applicant.getRole()).isEqualTo(Role.SUPPORTER);
        assertThat(applicant.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(applicant.getName()).isEqualTo("김젝트");
        assertThat(applicant.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(applicant.getSemesterId()).isEqualTo(1L);
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
        var requesterId = 2L;
        var memberId = 1L;
        var request = new AdminAccountActiveUpdateRequest(false);
        var applicant = Applicant.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .role(Role.OPERATIONS)
                .status(MemberStatus.ACTIVE)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(applicant);

        // when
        adminAccountService.updateActive(requesterId, memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        verify(adminMemberComponent).changeMemberStatus(eq(applicant), eq(MemberStatus.LOCKED));
    }

    @Test
    void 관리자_계정_활성화_성공() {
        // given
        var requesterId = 2L;
        var memberId = 1L;
        var request = new AdminAccountActiveUpdateRequest(true);
        var applicant = Applicant.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .role(Role.OPERATIONS)
                .status(MemberStatus.LOCKED)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId)).willReturn(applicant);

        // when
        adminAccountService.updateActive(requesterId, memberId, request);

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
        verify(adminMemberComponent).changeMemberStatus(eq(applicant), eq(MemberStatus.ACTIVE));
    }

    @Test
    void 관리자_계정_활성화_상태_일괄_수정_성공() {
        // given
        var requesterId = 3L;
        var firstRequest = new AdminAccountActiveUpdateRequest(1L, false);
        var secondRequest = new AdminAccountActiveUpdateRequest(2L, false);
        var firstApplicant = Applicant.builder()
                .id(firstRequest.memberId())
                .email("first@ject.kr")
                .role(Role.OPERATIONS)
                .status(MemberStatus.ACTIVE)
                .semesterId(1L)
                .build();
        var secondApplicant = Applicant.builder()
                .id(secondRequest.memberId())
                .email("second@ject.kr")
                .role(Role.SUPPORTER)
                .status(MemberStatus.ACTIVE)
                .semesterId(1L)
                .build();

        given(adminMemberComponent.getRequiredBackofficeMemberById(firstRequest.memberId()))
                .willReturn(firstApplicant);
        given(adminMemberComponent.getRequiredBackofficeMemberById(secondRequest.memberId()))
                .willReturn(secondApplicant);

        // when
        adminAccountService.updateActive(requesterId, List.of(firstRequest, secondRequest));

        // then
        verify(adminMemberComponent).getRequiredBackofficeMemberById(firstRequest.memberId());
        verify(adminMemberComponent).getRequiredBackofficeMemberById(secondRequest.memberId());
        verify(adminMemberComponent).changeMemberStatuses(eq(List.of(firstApplicant, secondApplicant)), eq(MemberStatus.LOCKED));
    }

    @Test
    void 관리자_계정_활성화_상태_일괄_수정_실패_본인_계정_비활성화() {
        // given
        var requesterId = 1L;
        var requests = List.of(
                new AdminAccountActiveUpdateRequest(2L, false),
                new AdminAccountActiveUpdateRequest(requesterId, false));

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(requesterId, requests))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.CANNOT_LOCK_SELF);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(any(Long.class));
    }

    @Test
    void 관리자_계정_활성화_상태_일괄_수정_실패_active_누락() {
        // given
        var request = new AdminAccountActiveUpdateRequest(1L, null);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(2L, List.of(request)))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ACTIVE);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(any(Long.class));
    }

    @Test
    void 관리자_계정_일괄_비활성화_실패_active_true() {
        // given
        var request = new AdminAccountActiveUpdateRequest(1L, true);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(2L, List.of(request)))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ACTIVE);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(any(Long.class));
    }

    @Test
    void 관리자_계정_일괄_비활성화_실패_memberId_누락() {
        // given
        var request = new AdminAccountActiveUpdateRequest(null, false);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(2L, List.of(request)))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ID);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(any(Long.class));
    }

    @Test
    void 관리자_계정_일괄_비활성화_실패_요청_항목_null() {
        // given
        var requests = Collections.singletonList((AdminAccountActiveUpdateRequest) null);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(2L, requests))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ID);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(any(Long.class));
    }

    @Test
    void 관리자_계정_활성화_상태_수정_실패_존재하지_않는_관리자() {
        // given
        var requesterId = 1L;
        var memberId = 999L;
        var request = new AdminAccountActiveUpdateRequest(false);

        given(adminMemberComponent.getRequiredBackofficeMemberById(memberId))
                .willThrow(new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(requesterId, memberId, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);

        verify(adminMemberComponent).getRequiredBackofficeMemberById(memberId);
    }

    @Test
    void 관리자_계정_활성화_상태_수정_실패_본인_계정_비활성화() {
        // given
        var requesterId = 1L;
        var memberId = 1L;
        var request = new AdminAccountActiveUpdateRequest(false);

        // when, then
        assertThatThrownBy(() -> adminAccountService.updateActive(requesterId, memberId, request))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.CANNOT_LOCK_SELF);

        verify(adminMemberComponent, never()).getRequiredBackofficeMemberById(memberId);
    }
}
