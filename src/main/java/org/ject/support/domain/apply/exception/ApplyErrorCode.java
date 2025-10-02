package org.ject.support.domain.apply.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum ApplyErrorCode implements ErrorCode {
    NOT_FOUND_APPLY(NOT_FOUND, "APPLY_NOT_FOUND", "지원 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}