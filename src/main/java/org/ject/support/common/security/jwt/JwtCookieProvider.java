package org.ject.support.common.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtCookieProvider {

    private final Environment environment;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Value("${security.cors.cookie.domain}")
    private String domain;

    public ResponseCookie createRefreshCookie(String refreshToken) { // TODO: 토큰 생성 로직 통합 및 토큰 정보 enum에서 관리하도록 리팩토링
        String cookieName = "refreshToken";
        return createCookie(cookieName, refreshToken);
    }

    public ResponseCookie createAccessCookie(String accessToken) {
        String cookieName = "accessToken";
        return createCookie(cookieName, accessToken);
    }

    public ResponseCookie createVerificationCookie(String verificationToken) {
        String cookieName = "verificationToken";
        return createCookie(cookieName, verificationToken);
    }

    private ResponseCookie createCookie(String cookieName, String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .domain(domain)
                .path("/")
                .maxAge(refreshExpirationTime / 1000)
                .httpOnly(true)
                .secure(true);

        // 운영은 기본 SameSite=Lax
        // 개발/QA 환경이면 cross-site 허용
        if (isDevOrLocal()) {
            builder.sameSite("None");
        }

        return builder.build();
    }

    private boolean isDevOrLocal() {
        return environment.acceptsProfiles(Profiles.of("dev", "local"));
    }
}
