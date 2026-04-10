package org.ject.support.admin.mail.exception;

import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.ErrorCode;

/**
 * 메일 도메인 전용 비즈니스 예외입니다.
 */
public class MailException extends BusinessException {
    public MailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
