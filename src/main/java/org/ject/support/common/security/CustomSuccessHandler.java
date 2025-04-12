package org.ject.support.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtCookieProvider;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieProvider jwtCookieProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.createAccessToken(authentication, customUserDetails.getMemberId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, customUserDetails.getMemberId());

        response.addCookie(jwtCookieProvider.createRefreshCookie(refreshToken));
        response.addCookie(jwtCookieProvider.createAccessCookie(accessToken));
    }

    public void onAuthenticationSuccess(HttpServletResponse response, String email) {
        String verificationToken = jwtTokenProvider.createVerificationToken(email);

        response.addCookie(jwtCookieProvider.createVerificationCookie(verificationToken));
    }

    public void onAuthenticationSuccess(HttpServletResponse response, String refreshToken, Long memberId) {
        String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken, memberId);

        response.addCookie(jwtCookieProvider.createAccessCookie(newAccessToken));
    }
}
