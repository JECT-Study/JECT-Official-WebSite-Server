package org.ject.support.domain.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.domain.admin.dto.AdminAuthSendRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendResponse;
import org.ject.support.domain.admin.dto.AdminVerifyRequest;
import org.ject.support.domain.admin.service.AdminAuthService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
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
    void 관리자_로그인1차_인증에_성공할_경우_이메일을_반환한다() {
        // given
        String adminLoginEmail = "id@email.com";
        AdminAuthSendRequest request = new AdminAuthSendRequest(adminLoginEmail);
        given(adminAuthService.sendAdminAuthCode(request.email()))
                .willReturn(adminLoginEmail);

        // when
        AdminAuthSendResponse result = adminAuthController.sendAdminAuthCode(request);

        // then
        verify(adminAuthService).sendAdminAuthCode(request.email());
        Assertions.assertThat(result.email()).isEqualTo(adminLoginEmail);
    }

    @Test
    void 관리자_로그인2차_인증에_성공할_경우_true를_반환한다() {
        // given
        String email = "test@ject.org";
        String code = "123456";
        AdminVerifyRequest request = new AdminVerifyRequest(email, code);
        when(adminAuthService.verifyAdminAuthCode(email, code)).thenReturn(authentication);

        // when
        boolean result = adminAuthController.verifyAdminAuthCode(
                request, httpServletRequest, httpServletResponse
        );

        // then
        assertTrue(result);
        verify(adminAuthService).verifyAdminAuthCode(email, code);
        verify(customSuccessHandler).onAuthenticationSuccess(
                httpServletRequest, httpServletResponse, authentication
        );
    }
}
