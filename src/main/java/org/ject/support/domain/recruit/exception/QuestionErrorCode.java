package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
@Getter
@AllArgsConstructor
public enum QuestionErrorCode implements ErrorCode {
    NOT_FOUND("QUESTION_NOT_FOUND", "해당 질문을 찾을 수 없습니다."),;

    private final String code;
    private final String message;
}
