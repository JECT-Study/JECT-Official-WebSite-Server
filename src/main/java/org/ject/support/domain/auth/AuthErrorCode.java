package org.ject.support.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_AUTH_CODE("INVALID_AUTH_CODE", "인증 번호가 유효하지 않습니다."),
    NOT_FOUND_AUTH_CODE("NOT_FOUND_AUTH_CODE", "인증 번호를 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN("EXPIRED_REFRESH_TOKEN", "만료된 리프레시 토큰입니다."),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "PIN 번호가 올바르지 않습니다.");

    private final String code;
    private final String message;
}
