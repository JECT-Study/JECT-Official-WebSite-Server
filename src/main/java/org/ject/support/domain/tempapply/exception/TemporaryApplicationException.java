package org.ject.support.domain.tempapply.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class TemporaryApplicationException extends BusinessException {
    public TemporaryApplicationException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
