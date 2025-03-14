package org.ject.support.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    NOT_FOUND_MEMBER("NOT_FOUND_MEMBER", "멤버를 찾을 수 없습니다."),
    ALREADY_EXIST_MEMBER("ALREADY_EXIST_MEMBER", "이미 가입되어 있는 회원입니다."),
    SAME_PIN("SAME_PIN", "기존과 같은 핀 번호입니다.");

    private final String code;
    private final String message;
}
