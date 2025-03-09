package org.ject.support.domain.file.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class FileException extends BusinessException {
    public FileException(ErrorCode errorCode) {
        super(errorCode);
    }
}
