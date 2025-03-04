package org.ject.support.domain.file.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ErrorCode {
    INVALID_EXTENSION("INVALID_EXTENSION", "유효하지 않은 확장자입니다."),
    ;

    private final String code;
    private final String message;
}