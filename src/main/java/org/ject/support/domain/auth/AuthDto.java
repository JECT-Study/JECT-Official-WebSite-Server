package org.ject.support.domain.auth;

public class AuthDto {

    public record VerifyAuthCodeRequest(String email, String authCode) {
        // 이메일 인증 코드 검증 요청 DTO
    }

    public record VerifyAuthCodeOnlyResponse(String verificationToken) {
        // 인증번호 검증 성공 시 발급되는 임시 토큰
    }
    
    public record TokenRefreshRequest(String refreshToken) {
        // 토큰 재발급 요청 DTO
    }
    
    public record TokenRefreshResponse(String accessToken) {
        // 토큰 재발급 응답 DTO
    }
}
