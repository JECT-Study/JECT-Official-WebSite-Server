package org.ject.support.domain.file.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ErrorCode {
    INVALID_EXTENSION(BAD_REQUEST, "INVALID_EXTENSION", "유효하지 않은 확장자입니다."),
    EXCEEDED_PORTFOLIO_MAX_SIZE(PAYLOAD_TOO_LARGE, "EXCEEDED_PORTFOLIO_SIZE", "첨부 가능한 포트폴리오 최대 용량을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}