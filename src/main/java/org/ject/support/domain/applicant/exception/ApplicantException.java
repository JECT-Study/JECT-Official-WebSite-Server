package org.ject.support.domain.applicant.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class ApplicantException extends BusinessException {
    public ApplicantException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
