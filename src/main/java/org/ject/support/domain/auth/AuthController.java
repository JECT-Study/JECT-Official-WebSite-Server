package org.ject.support.domain.auth;

import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.PinLoginRequest;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeRequest;
import org.ject.support.external.email.EmailTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 인증번호 검증 API
     * 인증번호 검증만 수행하고 임시 토큰을 발급합니다.
     */
    @PostMapping("/code")
    @PreAuthorize("permitAll()")
    public void verifyAuthCode(@RequestBody VerifyAuthCodeRequest verifyAuthCodeRequest, HttpServletRequest request,
                               HttpServletResponse response, @RequestParam EmailTemplate template) {
        // 서비스 레이어에서 템플릿 타입에 따른 인증 검증 결과 반환
        AuthVerificationResult result = authService.verifyAuthCodeByTemplate(
                verifyAuthCodeRequest.email(), verifyAuthCodeRequest.authCode(), template);
        
        // 결과에 따라 적절한 응답 처리
        if (result.hasAuthentication()) {
            customSuccessHandler.onAuthenticationSuccess(request, response, result.getAuthentication());
        } else {
            customSuccessHandler.onAuthenticationSuccess(response, result.getEmail());
        }
    }
    
    /**
     * 리프레시 토큰을 이용한 액세스 토큰 재발급 API
     * 리프레시 토큰이 유효한 경우 새로운 액세스 토큰을 발급합니다.
     */
    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        Long memberId = authService.refreshAccessToken(refreshToken);
        customSuccessHandler.onAuthenticationSuccess(response, refreshToken, memberId);
    }

    /**
     * PIN 로그인 API
     * 이메일과 PIN 번호로 로그인하고 액세스 토큰과 리프레시 토큰을 발급합니다.
     * CustomSuccessHandler를 통해 쿠키에 토큰을 저장합니다.
     */
    @PostMapping("/login/pin")
    @PreAuthorize("permitAll()")
    public void loginWithPin(@RequestBody @Valid PinLoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        Authentication authentication = authService.loginWithPin(request.email(), request.pin());
        
        customSuccessHandler.onAuthenticationSuccess(httpRequest, response, authentication);
    }

    @GetMapping("/login/exist")
    @PreAuthorize("permitAll()")
    public boolean isExistMember(@RequestParam String email) {
        return authService.isExistMember(email);
    }
}
