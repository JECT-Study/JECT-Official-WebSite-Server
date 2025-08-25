package org.ject.support.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    UNSUPPORTED_PARAMETER_TYPE(BAD_REQUEST, "G-01", "Unsupported parameter type"),
    RESOURCE_NOT_FOUND(NOT_FOUND, "G-02", "Resource not found"),
    TEMPLATE_NOT_FOUND(NOT_FOUND, "G-03", "Template file not found"),
    JSON_MARSHALLING_FAILURE(INTERNAL_SERVER_ERROR, "G-04", "Json marshalling failure"),
    EMPTY_ACCESS_TOKEN(UNAUTHORIZED, "G-05", "Empty access token"),
    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "G-06", "Invalid access token"),
    INVALID_PERMISSION(FORBIDDEN, "G-07", "Invalid permission"),
    AUTHENTICATION_REQUIRED(UNAUTHORIZED, "G-08", "Authentication is required"),
    OVER_PERIOD(CONFLICT, "G-09", "모집 기간이 아닙니다."),
    MISS_REQUIRED_REQUEST_PARAMETER(BAD_REQUEST, "G-10", "Missing required parameter"),
    MISS_REQUEST_BODY(BAD_REQUEST, "G-11", "Missing request body"),
    MISS_REQUIRED_JOB_FAMILY_PARAMETER(BAD_REQUEST, "G-12", "Missing required JobFamily parameter"),
    AUTHENTICATION_PROCESSING_ERROR(INTERNAL_SERVER_ERROR, "G-13", "인증 처리 중 오류가 발생했습니다."),
    REQUEST_METHOD_NOT_ALLOWED(METHOD_NOT_ALLOWED, "G-14", "Method not allowed");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
