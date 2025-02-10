package org.ject.support.common.security;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class SecurityException extends BusinessException {
    public SecurityException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
