package org.ject.support.domain.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum AdminErrorCode implements ErrorCode {
    NOT_FOUND_ADMIN("NOT_FOUND_ADMIN", "존재하지 않는 관리자 입니다."),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "너무 많은 요청입니다. 잠시 후 다시 시도해주세요.");

    private final String code;
    private final String message;
}
