package org.ject.support.external.email.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements ErrorCode {
    INVALID_EMAIL_TEMPLATE(BAD_REQUEST, "INVALID_EMAIL_TEMPLATE", "유효하지 않은 메일 템플릿입니다."),
    NOT_FOUND_SEND_GROUP(NOT_FOUND, "NOT_FOUND_SEND_GROUP", "존재하지 않는 전송 그룹입니다."),
    EMAIL_SEND_FAILURE(SERVICE_UNAVAILABLE, "EMAIL_SEND_FAILURE", "이메일 전송에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
