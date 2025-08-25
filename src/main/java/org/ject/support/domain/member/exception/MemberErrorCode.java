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
    NOT_FOUND_MEMBER(NOT_FOUND, "NOT_FOUND_MEMBER", "멤버를 찾을 수 없습니다."),
    ALREADY_EXIST_MEMBER(CONFLICT, "ALREADY_EXIST_MEMBER", "이미 가입되어 있는 회원입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
