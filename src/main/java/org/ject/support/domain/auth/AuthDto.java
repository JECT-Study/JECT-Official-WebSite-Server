package org.ject.support.domain.auth;

import lombok.Data;
import lombok.Getter;

public class AuthDto {

    @Data
    @Getter
    public static class AuthCodeResponse {
        private final String accessToken;
        private final String refreshToken;
    }
}
