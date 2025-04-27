package org.ject.support.external.email.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class EmailException extends BusinessException {
    public EmailException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
