package org.ject.support.admin.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.admin.auth.dto.AdminLoginRequest;
import org.ject.support.admin.auth.service.AdminAuthService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminAuthControllerTest {

    @InjectMocks
    private AdminAuthController adminAuthController;

    @Mock
    private AdminAuthService adminAuthService;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomSuccessHandler customSuccessHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Test
    void 관리자_로그인에_성공하면_true를_반환한다() {
        // given
        String email = "admin@ject.org";
        String password = "Password123";
        AdminLoginRequest request = new AdminLoginRequest(email, password);
        when(adminAuthService.authenticateAdmin(email, password)).thenReturn(authentication);

        // when
        boolean result = adminAuthController.loginAdmin(request, httpServletRequest, httpServletResponse);

        // then
        assertTrue(result);
        verify(adminAuthService).authenticateAdmin(email, password);
        verify(customSuccessHandler).onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);
    }

    @Test
    void 이메일_형식이_올바르지_않은_경우_유효성_검사에_실패한다() {
        // given
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        AdminLoginRequest request = new AdminLoginRequest("invalid-email", "Password123");

        // when
        Set<ConstraintViolation<AdminLoginRequest>> violations = validator.validate(request);

        // then
        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().equals("올바른 이메일 형식이 아닙니다."));
    }

    @Test
    void 비밀번호_길이가_짧은_경우_유효성_검사에_실패한다() {
        // given
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        AdminLoginRequest request = new AdminLoginRequest("admin@ject.org", "123");

        // when
        Set<ConstraintViolation<AdminLoginRequest>> violations = validator.validate(request);

        // then
        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().equals("비밀번호는 8자 이상 입력해주세요."));
    }

    @Test
    void 관리자_로그아웃에_성공할_경우_쿠키를_만료시킨다() {
        // when
        adminAuthController.logoutAdmin(httpServletResponse);

        // then
        verify(customSuccessHandler).onLogoutSuccess(httpServletResponse);
    }
}
