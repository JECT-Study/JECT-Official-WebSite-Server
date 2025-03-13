package org.ject.support.domain.auth;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.auth.AuthDto.TokenRefreshRequest;
import org.ject.support.domain.auth.AuthDto.TokenRefreshResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 인증번호 검증 API
     * 인증번호 검증만 수행하고 임시 토큰을 발급합니다.
     */
    @PostMapping("/code")
    @PreAuthorize("permitAll()")
    public VerifyAuthCodeOnlyResponse verifyAuthCode(@RequestBody VerifyAuthCodeRequest request) {
        return authService.verifyEmailByAuthCodeOnly(request.email(), request.authCode());
    }
    
    /**
     * 리프레시 토큰을 이용한 액세스 토큰 재발급 API
     * 리프레시 토큰이 유효한 경우 새로운 액세스 토큰을 발급합니다.
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public TokenRefreshResponse refreshToken(@AuthPrincipal Long memberId, @RequestBody TokenRefreshRequest request) {
        return authService.refreshAccessToken(memberId, request.refreshToken());
    }
}
