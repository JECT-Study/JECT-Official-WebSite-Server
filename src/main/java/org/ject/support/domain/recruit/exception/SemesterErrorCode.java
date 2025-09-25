package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum SemesterErrorCode implements ErrorCode {
    NOT_FOUND_RECRUITING_SEMESTER(NOT_FOUND, "NOT_FOUND_RECRUITING_SEMESTER", "현재 모집중인 기수가 존재하지 않습니다."),
    NOT_FOUND_SEMESTER(NOT_FOUND, "NOT_FOUND_SEMESTER", "기수가 존재하지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
