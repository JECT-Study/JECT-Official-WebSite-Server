package org.ject.support.domain.admin.exception;

import org.ject.support.common.exception.BusinessException;

public class AdminException extends BusinessException {
    public AdminException(final AdminErrorCode errorCode) {
        super(errorCode);
    }
}
