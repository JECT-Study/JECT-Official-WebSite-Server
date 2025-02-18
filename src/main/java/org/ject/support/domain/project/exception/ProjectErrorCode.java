package org.ject.support.domain.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum ProjectErrorCode implements ErrorCode {
    NOT_FOUND("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."),
    ;

    private final String code;
    private final String message;
}
