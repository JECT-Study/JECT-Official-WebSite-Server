package org.ject.support.domain.auth;

import lombok.Getter;
import org.springframework.security.core.Authentication;

/**
 * 인증 검증 결과를 담는 클래스
 * 템플릿 타입에 따라 다른 결과를 반환하기 위한 래퍼 클래스
 */

@Getter
public class AuthVerificationResult {
    private final Authentication authentication;
    private final String email;

    // AUTH_CODE 템플릿용 생성자
    public AuthVerificationResult(Authentication authentication) {
        this.authentication = authentication;
        this.email = null;
    }

    // PIN_RESET 템플릿용 생성자
    public AuthVerificationResult(String email) {
        this.authentication = null;
        this.email = email;
    }

    public boolean hasAuthentication() {
        return authentication != null;
    }
}