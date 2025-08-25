package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum QuestionErrorCode implements ErrorCode {
    NOT_FOUND_QUESTION(NOT_FOUND, "QUESTION_NOT_FOUND", "해당 질문을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
