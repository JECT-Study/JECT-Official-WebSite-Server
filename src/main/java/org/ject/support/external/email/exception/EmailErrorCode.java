package org.ject.support.external.email.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements ErrorCode {
    INVALID_EMAIL_TEMPLATE("INVALID_MAIL_TEMPLATE", "유효하지 않은 메일 템플릿입니다."),;

    private final String code;
    private final String message;
}
