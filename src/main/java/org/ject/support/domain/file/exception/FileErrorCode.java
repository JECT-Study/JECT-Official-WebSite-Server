package org.ject.support.domain.file.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ErrorCode {
    INVALID_EXTENSION("INVALID_EXTENSION", "유효하지 않은 확장자입니다."),
    EXCEEDED_PORTFOLIO_MAX_SIZE("EXCEEDED_PORTFOLIO_SIZE", "첨부 가능한 포트폴리오 최대 용량을 초과했습니다.");

    private final String code;
    private final String message;
}