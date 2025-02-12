package org.ject.support.domain.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.auth.AuthDto.AuthCodeResponse;
import org.ject.support.external.email.EmailDto.VerifyAuthCodeRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/verify-auth-code")
    public AuthCodeResponse verifyAuthCode(HttpServletResponse response,
                                           @RequestBody VerifyAuthCodeRequest verifyAuthCodeRequest) {
        return authService.verifyEmailByAuthCode(response, verifyAuthCodeRequest.getEmail(),
                verifyAuthCodeRequest.getAuthCode());
    }
}
