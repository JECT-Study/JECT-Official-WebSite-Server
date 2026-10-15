package org.ject.support.common.security;

import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.jwt.JwtCookieProvider;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;

class CustomSuccessHandlerTest extends UnitTestSupport {

    @InjectMocks
    private CustomSuccessHandler customSuccessHandler;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtCookieProvider jwtCookieProvider;

    @Test
    @DisplayName("이메일 인증에 성공하면 기존 로그인 쿠키를 삭제하고 인증 쿠키를 발급한다")
    void 이메일_인증에_성공하면_기존_로그인_쿠키를_삭제하고_인증_쿠키를_발급한다() {
        // given
        String email = "test@example.com";
        String verificationToken = "verification-token";
        var response = new MockHttpServletResponse();
        var verificationCookie = ResponseCookie.from("verificationToken", verificationToken).build();
        given(jwtTokenProvider.createVerificationToken(email)).willReturn(verificationToken);
        given(jwtCookieProvider.createVerificationCookie(verificationToken)).willReturn(verificationCookie);

        // when
        customSuccessHandler.onAuthenticationSuccess(response, email);

        // then
        verify(jwtCookieProvider).deleteLoginCookies(response);
        verify(jwtCookieProvider).createVerificationCookie(verificationToken);
        org.assertj.core.api.Assertions.assertThat(response.getHeaders("Set-Cookie"))
                .contains(verificationCookie.toString());
    }
}
