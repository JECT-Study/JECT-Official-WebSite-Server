package org.ject.support.external.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum MailErrorCode implements ErrorCode {
    MAIL_SEND_FAILURE("MAIL_SEND_FAILURE", "메일 전송에 실패하였습니다."),
    MAIL_PARAMETER_PARSE_FAILURE("MAIL_PARAMETER_PARSE_FAILURE", "메일 생성을 위한 객체 변환에 실패했습니다."),
    INVALID_MAIL_TEMPLATE("INVALID_MAIL_TEMPLATE", "유효하지 않은 메일 템플릿입니다."),
    ;
    private final String code;
    private final String message;
}
