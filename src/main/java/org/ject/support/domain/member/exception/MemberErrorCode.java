package org.ject.support.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    NOT_FOUND_MEMBER(NOT_FOUND, "MEMBER-1", "멤버를 찾을 수 없습니다."),
    ALREADY_EXIST_MEMBER(CONFLICT, "MEMBER-2", "이미 가입되어 있는 회원입니다."),
    NOT_FOUND_SEMESTER_OF_MEMBER(NOT_FOUND, "MEMBER-3", "회원의 기수를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(CONFLICT, "MEMBER-4", "이미 사용 중인 이메일입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
