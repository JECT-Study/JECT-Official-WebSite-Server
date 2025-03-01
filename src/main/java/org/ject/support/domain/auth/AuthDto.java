package org.ject.support.domain.auth;

public class AuthDto {

    public record VerifyAuthCodeRequest(String name, String email, String phoneNumber, String authCode) {
    }

    public record AuthCodeResponse(String accessToken, String refreshToken) {
    }
}
