package org.ject.support.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdminErrorCode implements ErrorCode {
    NOT_FOUND_ADMIN(HttpStatus.NOT_FOUND, "ADMIN-1", "존재하지 않는 관리자 입니다."),
    LOCKED_ADMIN(HttpStatus.FORBIDDEN, "ADMIN-5", "계정이 비활성화되어 있습니다. 관리자에게 문의해주세요."),
    INVALID_ADMIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "ADMIN-6", "이메일 또는 비밀번호가 올바르지 않습니다."),
    LOGIN_ATTEMPT_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "ADMIN-7", "로그인 시도가 제한되었습니다. 잠시 후 다시 시도해주세요."),
    DUPLICATE_ADMIN_EMAIL(HttpStatus.CONFLICT, "ADMIN-8", "이미 사용중인 이메일 입니다."),
    INVALID_ADMIN_ACCOUNT_ROLE(HttpStatus.BAD_REQUEST, "ADMIN-9", "관리자 계정 유형만 선택할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
