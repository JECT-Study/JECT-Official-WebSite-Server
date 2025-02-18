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
    AUTHENTICATION_REQUIRED("G-09", "Authentication is required"),
    INTERNAL_SERVER_ERROR("G-10", "Internal server error"),
    OVER_PERIOD("G-06", "모집 기간이 아닙니다.");

    private final String code;
    private final String message;
}
