package org.ject.support.admin.mail.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

/**
 * 메일 도메인에서 사용하는 에러 코드/HTTP 상태 정의입니다.
 */
@Getter
@AllArgsConstructor
public enum MailErrorCode implements ErrorCode {
    SCENARIO_NOT_FOUND(NOT_FOUND, "MAIL-1", "메일 발송 시나리오를 찾을 수 없습니다."),
    INVALID_TEMPLATE_SYNTAX(BAD_REQUEST, "MAIL-2", "템플릿 문법이 올바르지 않습니다."),
    UNSUPPORTED_TEMPLATE_VARIABLE(BAD_REQUEST, "MAIL-3", "허용되지 않은 템플릿 변수가 포함되어 있습니다."),
    MISSING_REQUIRED_COMMON_VARIABLE(BAD_REQUEST, "MAIL-4", "필수 공통 변수가 누락되었습니다."),
    DUPLICATE_SCENARIO_CODE(CONFLICT, "MAIL-5", "이미 사용 중인 시나리오 코드입니다."),
    INACTIVE_SCENARIO(BAD_REQUEST, "MAIL-6", "비활성화된 시나리오입니다."),
    RECEIVER_NOT_FOUND(NOT_FOUND, "MAIL-7", "수신자 정보를 찾을 수 없습니다."),
    EMPTY_RECEIVERS(BAD_REQUEST, "MAIL-8", "수신자 목록은 비어 있을 수 없습니다."),
    DISPATCH_JOB_NOT_FOUND(NOT_FOUND, "MAIL-9", "메일 발송 작업을 찾을 수 없습니다."),
    INVALID_DISPATCH_JOB_STATUS(BAD_REQUEST, "MAIL-10", "현재 상태에서는 발송 실행을 수행할 수 없습니다."),
    TEST_MAIL_SEND_FAILURE(SERVICE_UNAVAILABLE, "MAIL-11", "테스트 메일 발송에 실패했습니다."),
    INVALID_VARIABLE_VALUE(BAD_REQUEST, "MAIL-12", "메일 입력 변수 값이 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
