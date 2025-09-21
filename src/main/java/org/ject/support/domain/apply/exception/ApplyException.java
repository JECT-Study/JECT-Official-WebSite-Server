package org.ject.support.domain.apply.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class ApplyException extends BusinessException {
    public ApplyException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
