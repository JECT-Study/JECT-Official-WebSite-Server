package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum SemesterErrorCode implements ErrorCode {
    ONGOING_SEMESTER_NOT_FOUND(NOT_FOUND, "ONGOING_SEMESTER_NOT_FOUND", "Ongoing semester not found."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
