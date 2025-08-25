package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

@Getter
@AllArgsConstructor
public enum ApplyErrorCode implements ErrorCode {
    DUPLICATE_JOB_FAMILY(CONFLICT, "DUPLICATE_JOB_FAMILY", "변경하려는 직군이 기존 직군과 동일합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
