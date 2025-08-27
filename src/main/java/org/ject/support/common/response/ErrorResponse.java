package org.ject.support.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private ErrorCode code;
    private List<String> messages;

    public static ErrorResponse of(ErrorCode code) {
        return new ErrorResponse(code, List.of(code.getMessage()));
    }

    public static ErrorResponse of(ErrorCode code, List<String> messages) {
        return new ErrorResponse(code, messages);
    }

    public HttpStatus getStatus() {
        return code.getHttpStatus();
    }

    public String getCode() {
        return code.getCode();
    }

    public List<String> getMessages() {
        return messages;
    }
}
