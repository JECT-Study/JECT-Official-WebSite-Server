package org.ject.support.domain.recruit.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class QuestionException extends BusinessException {
    public QuestionException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
