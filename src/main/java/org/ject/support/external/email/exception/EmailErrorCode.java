package org.ject.support.external.email.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements ErrorCode {
    INVALID_EMAIL_TEMPLATE(BAD_REQUEST, "INVALID_EMAIL_TEMPLATE", "유효하지 않은 메일 템플릿입니다."),
    EMAIL_SEND_FAILURE(SERVICE_UNAVAILABLE, "EMAIL_SEND_FAILURE", "이메일 전송에 실패했습니다."),
    TOO_MANY_EMAIL_REQUESTS(TOO_MANY_REQUESTS, "TOO_MANY_EMAIL_REQUESTS", "이메일 전송 요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
