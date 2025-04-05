package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum SemesterErrorCode implements ErrorCode {
    ONGOING_SEMESTER_NOT_FOUND("ONGOING_SEMESTER_NOT_FOUND", "Ongoing semester not found."),;

    private final String code;
    private final String message;
}
