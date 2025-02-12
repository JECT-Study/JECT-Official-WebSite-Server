package org.ject.support.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    UNSUPPORTED_PARAMETER_TYPE("G-01", "Unsupported parameter type"),
    RESOURCE_NOT_FOUND("G-02", "Resource not found"),
    METHOD_NOT_ALLOWED("G-03", "Method not allowed"),
    TEMPLATE_NOT_FOUND("G-04", "Template file not found"),
    JSON_MARSHALLING_FAILURE("G-05", "Json marshalling failure"),
    EMPTY_ACCESS_TOKEN("G-06", "Empty access token"),
    INVALID_ACCESS_TOKEN("G-07", "Invalid access token"),
    INVALID_PERMISSION("G-08", "Invalid permission"),
    ;

    private final String code;
    private final String message;
}
