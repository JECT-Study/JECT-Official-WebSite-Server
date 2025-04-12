package org.ject.support.common.security.jwt;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtCookieProvider {

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Value("${security.cors.cookie.domain}")
    private String domain;

    public Cookie createRefreshCookie(String refreshToken) { // TODO: 토큰 생성 로직 통합 및 토큰 정보 enum에서 관리하도록 리팩토링
        String cookieName = "refreshToken";
        return createCookie(cookieName, refreshToken);
    }

    public Cookie createAccessCookie(String accessToken) {
        String cookieName = "accessToken";
        return createCookie(cookieName, accessToken);
    }

    public Cookie createVerificationCookie(String verificationToken) {
        String cookieName = "verificationToken";
        return createCookie(cookieName, verificationToken);
    }

    private Cookie createCookie(String cookieName, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge((int)(refreshExpirationTime / 1000));

        return cookie;
    }
}
