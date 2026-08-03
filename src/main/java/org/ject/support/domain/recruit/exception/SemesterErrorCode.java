package org.ject.support.domain.recruit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum SemesterErrorCode implements ErrorCode {
    NOT_FOUND_RECRUITING_SEMESTER(NOT_FOUND, "SEMESTER-1", "현재 모집중인 기수가 존재하지 않습니다."),
    NOT_FOUND_SEMESTER(NOT_FOUND, "SEMESTER-2", "기수가 존재하지 않습니다."),
    NOT_FOUND_SEMESTER_EVENT(NOT_FOUND, "SEMESTER-3", "기수의 행사를 찾을 수 없습니다."),
    DUPLICATED_SEMESTER_EVENT_ID(BAD_REQUEST, "SEMESTER-4", "중복된 행사 ID가 존재합니다."),
    EXCEEDED_SEMESTER_EVENT_LIMIT(BAD_REQUEST, "SEMESTER-5", "기수별 행사 유형은 최대 10개까지 등록할 수 있습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
