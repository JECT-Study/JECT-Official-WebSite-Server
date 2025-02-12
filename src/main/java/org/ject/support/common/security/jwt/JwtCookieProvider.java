package org.ject.support.common.security.jwt;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtCookieProvider {
    
    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    public Cookie createRefreshCookie(String refreshToken) {
        String cookieName = "refreshToken";
        Cookie cookie = new Cookie(cookieName, refreshToken);
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int)(refreshExpirationTime / 1000));
        return cookie;
    }

    public Cookie createAccessCookie(String accessToken) {
        String cookieName = "accessToken";
        Cookie cookie = new Cookie(cookieName, accessToken);
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); TODO : HTTPS 적용 시 적용 가능
        cookie.setPath("/");
        cookie.setMaxAge((int)(accessExpirationTime / 1000));
        return cookie;
    }
}
