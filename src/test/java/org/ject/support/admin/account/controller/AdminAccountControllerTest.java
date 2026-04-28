package org.ject.support.admin.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.admin.account.dto.AdminAccountActiveUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.dto.AdminAccountRoleUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountUpdateRequest;
import org.ject.support.admin.account.service.AdminAccountService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.security.AuthenticatedMemberIdResolver;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAccountControllerTest extends UnitTestSupport {

    @InjectMocks
    private AdminAccountController adminAccountController;

    @Mock
    private AdminAccountService adminAccountService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(adminAccountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticatedMemberIdResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 관리자_계정_생성_성공() throws Exception {
        // given
        var request = new AdminAccountCreateRequest("admin@ject.kr", "password123", "김젝트", Role.OPERATIONS);

        doNothing().when(adminAccountService).createAccount(any(AdminAccountCreateRequest.class));

        // when, then
        mockMvc.perform(post("/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(adminAccountService).createAccount(any(AdminAccountCreateRequest.class));
    }

    @Test
    void 관리자_계정_생성은_ADMIN_권한만_허용한다() throws Exception {
        // when
        PreAuthorize preAuthorize = AdminAccountController.class
                .getMethod("createAccount", AdminAccountCreateRequest.class)
                .getAnnotation(PreAuthorize.class);

        // when, then
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ROLE_ADMIN')");
    }

    @Test
    void 관리자_계정_정보_수정_성공() throws Exception {
        // given
        var requesterId = 2L;
        var memberId = 1L;
        var request = new AdminAccountUpdateRequest("이젝트", Role.SUPPORTER, false);
        setAuthentication(requesterId);

        doNothing().when(adminAccountService)
                .updateAccount(eq(requesterId), eq(memberId), any(AdminAccountUpdateRequest.class));

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(adminAccountService)
                .updateAccount(eq(requesterId), eq(memberId), any(AdminAccountUpdateRequest.class));
    }

    @Test
    void 관리자_계정_정보_수정은_ADMIN_권한만_허용한다() throws Exception {
        // when
        PreAuthorize preAuthorize = AdminAccountController.class
                .getMethod("updateAccount", Long.class, Long.class, AdminAccountUpdateRequest.class)
                .getAnnotation(PreAuthorize.class);

        // when, then
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ROLE_ADMIN')");
    }

    @Test
    void 관리자_계정_정보_수정_실패_role_누락() throws Exception {
        // given
        var request = new AdminAccountUpdateRequest("김젝트", null, true);
        setAuthentication(2L);

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 관리자_계정_정보_수정_실패_active_누락() throws Exception {
        // given
        var request = new AdminAccountUpdateRequest("김젝트", Role.OPERATIONS, null);
        setAuthentication(2L);

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 관리자_계정_권한_수정_성공() throws Exception {
        // given
        var memberId = 1L;
        var request = new AdminAccountRoleUpdateRequest(Role.SUPPORTER);

        doNothing().when(adminAccountService).updateRole(eq(memberId), any(AdminAccountRoleUpdateRequest.class));

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}/role", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(adminAccountService).updateRole(eq(memberId), any(AdminAccountRoleUpdateRequest.class));
    }

    @Test
    void 관리자_계정_권한_수정은_ADMIN_권한만_허용한다() throws Exception {
        // when
        PreAuthorize preAuthorize = AdminAccountController.class
                .getMethod("updateRole", Long.class, AdminAccountRoleUpdateRequest.class)
                .getAnnotation(PreAuthorize.class);

        // when, then
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ROLE_ADMIN')");
    }

    @Test
    void 관리자_계정_활성화_상태_수정_성공() throws Exception {
        // given
        var requesterId = 2L;
        var memberId = 1L;
        var request = new AdminAccountActiveUpdateRequest(false);
        setAuthentication(requesterId);

        doNothing().when(adminAccountService)
                .updateActive(eq(requesterId), eq(memberId), any(AdminAccountActiveUpdateRequest.class));

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}/active", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(adminAccountService).updateActive(eq(requesterId), eq(memberId), any(AdminAccountActiveUpdateRequest.class));
    }

    @Test
    void 관리자_계정_활성화_상태_수정은_ADMIN_권한만_허용한다() throws Exception {
        // when
        PreAuthorize preAuthorize = AdminAccountController.class
                .getMethod("updateActive", Long.class, Long.class, AdminAccountActiveUpdateRequest.class)
                .getAnnotation(PreAuthorize.class);

        // when, then
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ROLE_ADMIN')");
    }

    @Test
    void 관리자_계정_활성화_상태_수정_실패_active_누락() throws Exception {
        // given
        var request = new AdminAccountActiveUpdateRequest(null);
        setAuthentication(2L);

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}/active", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.METHOD_VALIDATION_FAILED.getCode()))
                .andDo(print());
    }

    private void setAuthentication(final Long memberId) {
        CustomUserDetails userDetails = new CustomUserDetails("admin@ject.kr", memberId, Role.ADMIN);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void 관리자_계정_권한_수정_실패_role_누락() throws Exception {
        // given
        var request = new AdminAccountRoleUpdateRequest(null);

        // when, then
        mockMvc.perform(patch("/admin/accounts/{memberId}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.METHOD_VALIDATION_FAILED.getCode()))
                .andDo(print());
    }

    @Test
    void 관리자_계정_생성_실패_올바르지_않은_이메일_형식() throws Exception {
        // given
        var request = new AdminAccountCreateRequest("invalid-email", "password123", "김젝트", Role.OPERATIONS);

        // when, then
        mockMvc.perform(post("/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.METHOD_VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.messages[0]").value("올바른 이메일 형식이 아닙니다."))
                .andDo(print());
    }

    @Test
    void 관리자_계정_생성_실패_이메일_길이_초과() throws Exception {
        // given
        var request = new AdminAccountCreateRequest(
                "very-long-admin-email-test@ject.kr",
                "password123",
                "김젝트",
                Role.OPERATIONS
        );

        // when, then
        mockMvc.perform(post("/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.METHOD_VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.messages[0]").value("이메일 길이는 최대 30자리 까지 가능합니다."))
                .andDo(print());
    }

    @Test
    void 관리자_계정_생성_실패_이름_길이_초과() throws Exception {
        // given
        var request = new AdminAccountCreateRequest(
                "admin@ject.kr",
                "password123",
                "김젝트김젝트김젝트김젝트김젝트김젝트김젝트",
                Role.OPERATIONS
        );

        // when, then
        mockMvc.perform(post("/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.METHOD_VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.messages[0]").value("이름 길이는 최대 20자리 까지 가능합니다."))
                .andDo(print());
    }
}
