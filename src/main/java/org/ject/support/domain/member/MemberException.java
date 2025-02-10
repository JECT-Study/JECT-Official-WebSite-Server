package org.ject.support.domain.member;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

public class MemberException extends BusinessException {
    public MemberException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
