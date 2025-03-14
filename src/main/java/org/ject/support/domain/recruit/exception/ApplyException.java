package org.ject.support.domain.recruit.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class ApplyException extends BusinessException {
    public ApplyException(ErrorCode errorCode) {
        super(errorCode);
    }
}
