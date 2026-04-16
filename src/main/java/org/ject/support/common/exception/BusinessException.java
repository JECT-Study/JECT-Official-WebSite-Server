package org.ject.support.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 예외를 처리하기 위한 클래스
 * <br/>
 * 예외 발생 시 {@link ErrorCode}의 구현체인 enum을 전달하여 처리
 * <br/>
 * 비즈니스 로직상 발생한 예외는 해당 클래스를 구현한 예외를 발생시키도록 강제되어야함
 * @see GlobalExceptionHandler
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
