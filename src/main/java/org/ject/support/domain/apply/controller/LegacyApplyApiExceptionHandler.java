package org.ject.support.domain.apply.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.response.ErrorResponse;
import org.ject.support.domain.recruit.controller.QuestionController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {ApplyController.class, QuestionController.class})
public class LegacyApplyApiExceptionHandler {
    private static final String RECRUIT_ID_PARAMETER = "recruitId";

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse handleMissingRequestParameter(MissingServletRequestParameterException e,
                                                       HttpServletRequest request) {
        if (RECRUIT_ID_PARAMETER.equals(e.getParameterName())) {
            log.warn("Deprecated legacy apply API request without recruitId. method={}, uri={}, queryString={}",
                    request.getMethod(), request.getRequestURI(), request.getQueryString());
        }

        return ErrorResponse.of(GlobalErrorCode.MISS_REQUIRED_REQUEST_PARAMETER);
    }
}
