package org.ject.support.domain.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdminErrorCode implements ErrorCode {
    NOT_FOUND_ADMIN(HttpStatus.NOT_FOUND, "ADMIN-1", "존재하지 않는 관리자 입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "ADMIN-2", "너무 많은 요청입니다. 잠시 후 다시 시도해주세요."),
    NOT_FOUND_AUTH_CODE(HttpStatus.NOT_FOUND, "ADMIN-3", "인증 코드가 존재하지 않습니다."),
    INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, "ADMIN-4", "인증 코드가 올바르지 않습니다."),
    LOCKED_ADMIN(HttpStatus.FORBIDDEN, "ADMIN-5", "잠긴 관리자 계정입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
