package org.ject.support.domain.recruit.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class RecruitException extends BusinessException {
    public RecruitException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
