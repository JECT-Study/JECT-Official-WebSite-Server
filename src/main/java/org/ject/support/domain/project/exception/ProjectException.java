package org.ject.support.domain.project.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class ProjectException extends BusinessException {
    public ProjectException(ErrorCode errorCode) {
        super(errorCode);
    }
}
