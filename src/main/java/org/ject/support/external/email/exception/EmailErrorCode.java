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
    INVALID_EMAIL_TEMPLATE(BAD_REQUEST, "EMAIL-1", "유효하지 않은 메일 템플릿입니다."),
    EMAIL_SEND_FAILURE(SERVICE_UNAVAILABLE, "EMAIL-2", "이메일 전송에 실패했습니다."),
    EMAIL_SEND_RESULT_UNKNOWN(SERVICE_UNAVAILABLE, "EMAIL-3", "이메일 발송 결과를 확인할 수 없습니다."),
    TOO_MANY_EMAIL_REQUESTS(TOO_MANY_REQUESTS, "EMAIL-4", "이메일 전송 요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public static String normalizeCode(String code) {
        // 과거 발송 이력은 수정하지 않고 조회 시 알려진 기존 코드만 현재 코드로 변환한다.
        for (EmailErrorCode errorCode : values()) {
            if (errorCode.name().equals(code)) {
                return errorCode.getCode();
            }
        }
        return code;
    }
}
