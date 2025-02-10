package org.ject.support.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum SecurityErrorCode implements ErrorCode {
    EMPTY_ACCESS_TOKEN("EMPTY_ACCESS_TOKEN", "Access Token이 존재하지 않습니다."),
    INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "Access Token이 유효하지 않습니다.");

    private final String code;
    private final String message;
}
