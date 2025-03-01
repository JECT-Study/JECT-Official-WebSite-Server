package org.ject.support.domain.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtCookieProvider;
import org.ject.support.domain.auth.AuthDto.AuthCodeResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtCookieProvider jwtCookieProvider;

    @PostMapping("/code")
    public AuthCodeResponse verifyAuthCode(HttpServletResponse response,
                                           @RequestBody VerifyAuthCodeRequest request) {
        AuthCodeResponse authResponse = authService.verifyEmailByAuthCode(request.name(), request.email(),
                request.phoneNumber(), request.authCode());

        response.addCookie(jwtCookieProvider.createAccessCookie(authResponse.accessToken()));
        response.addCookie(jwtCookieProvider.createRefreshCookie(authResponse.refreshToken()));

        return authResponse;
    }
}
