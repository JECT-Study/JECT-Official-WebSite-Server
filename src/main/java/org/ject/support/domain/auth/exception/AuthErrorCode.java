package org.ject.support.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_AUTH_CODE(UNAUTHORIZED, "AUTH-1", "인증 번호가 유효하지 않습니다."),
    NOT_FOUND_AUTH_CODE(UNAUTHORIZED, "AUTH-2", "인증 번호를 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "AUTH-3", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "AUTH-4", "만료된 리프레시 토큰입니다."),
    INVALID_CREDENTIALS(UNAUTHORIZED, "AUTH-5", "PIN 번호가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
