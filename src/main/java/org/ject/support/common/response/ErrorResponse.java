package org.ject.support.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    public HttpStatus getStatus() {
        return code.getHttpStatus();
    }

    @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    public String getCode() {
        return code.getCode();
    }

    @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    public List<String> getMessages() {
        return messages;
    }
}
