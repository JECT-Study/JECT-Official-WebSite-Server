package org.ject.support.domain.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDto {

    public record VerifyAuthCodeRequest(String email, String authCode) {
        // 이메일 인증 코드 검증 요청 DTO
    }

    public record VerifyAuthCodeOnlyResponse(String verificationToken) {
        // 인증번호 검증 성공 시 발급되는 임시 토큰과 기존 회원 여부
    }
    
    public record PinLoginRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "PIN 번호는 6자리 숫자여야 합니다.") String pin) {
        // PIN 로그인 요청 DTO
    }
    
    public record PinLoginResponse(String accessToken, String refreshToken) {
        // 로그인 성공 시 발급되는 토큰 응답 DTO
    }
}
