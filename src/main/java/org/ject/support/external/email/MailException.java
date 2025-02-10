package org.ject.support.external.email;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class MailException extends BusinessException {
    public MailException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
