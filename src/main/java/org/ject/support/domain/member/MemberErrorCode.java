package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    NOT_FOUND_MEMBER("NOT_FOUND_MEMBER", "멤버를 찾을 수 없습니다.");
    private final String code;
    private final String message;
}
