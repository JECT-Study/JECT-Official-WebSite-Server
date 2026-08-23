package org.ject.support.domain.apply.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum ApplyErrorCode implements ErrorCode {
    NOT_FOUND_APPLY(NOT_FOUND, "APPLY-1", "지원 정보를 찾을 수 없습니다."),
    ALREADY_SUBMITTED(CONFLICT, "APPLY-2", "이미 지원서를 제출한 상태입니다."),
    NOT_FOUND_TEMP_APPLICATION_FORM(NOT_FOUND, "APPLY-3", "임시 저장한 지원서가 존재하지 않습니다."),
    NOT_FOUND_SUBMITTED_APPLICATION_FORM(NOT_FOUND, "APPLY-4", "제출된 지원서가 존재하지 않습니다."),
    NOT_SUBMITTED(CONFLICT, "APPLY-5", "제출 완료된 지원서가 아닙니다."),
    PORTFOLIO_REQUIRED(BAD_REQUEST, "APPLY-6", "포트폴리오를 필수로 제출해야 합니다."),
    WAITLIST_NUMBER_REQUIRED(BAD_REQUEST, "APPLY-7", "예비 합격은 예비 번호를 함께 지정해야 합니다."),
    WAITLIST_NUMBER_NOT_ALLOWED(BAD_REQUEST, "APPLY-8", "예비 합격이 아닌 선정 결과에는 예비 번호를 지정할 수 없습니다."),
    DUPLICATE_WAITLIST_NUMBER(CONFLICT, "APPLY-9", "같은 모집 공고 안에서 예비 번호는 중복될 수 없습니다."),
    DUPLICATE_APPLY_ID(BAD_REQUEST, "APPLY-10", "하나의 요청에 같은 지원을 두 번 담을 수 없습니다."),
    INVALID_WAITLIST_NUMBER(BAD_REQUEST, "APPLY-11", "예비 번호는 1 이상의 정수여야 합니다."),
    APPLY_EXISTS_IN_OTHER_RECRUIT(CONFLICT, "APPLY-12", "다른 공고에서 작성 중인 지원서가 있습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
