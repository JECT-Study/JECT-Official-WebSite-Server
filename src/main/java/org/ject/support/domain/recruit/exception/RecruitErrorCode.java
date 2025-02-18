package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum RecruitErrorCode implements ErrorCode {
    NOT_FOUND("RECRUIT_NOT_FOUND", "모집 공고를 찾을 수 없습니다."),;
    private final String code;
    private final String message;
}
