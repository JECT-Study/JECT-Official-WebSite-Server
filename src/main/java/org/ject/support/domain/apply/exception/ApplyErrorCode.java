package org.ject.support.domain.apply.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum ApplyErrorCode implements ErrorCode {
    NOT_FOUND_APPLY(NOT_FOUND, "APPLY-1", "지원 정보를 찾을 수 없습니다."),
    ALREADY_SUBMITTED(CONFLICT, "APPLY-2", "이미 지원서를 제출한 상태입니다."),
    NOT_FOUND_TEMP_APPLICATION_FORM(NOT_FOUND, "APPLY-3", "임시 저장한 지원서가 존재하지 않습니다."),
    NOT_FOUND_SUBMITTED_APPLICATION_FORM(NOT_FOUND, "APPLY-4", "제출된 지원서가 존재하지 않습니다."),
    NOT_SUBMITTED(CONFLICT, "APPLY-5", "제출 완료된 지원서가 아닙니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
