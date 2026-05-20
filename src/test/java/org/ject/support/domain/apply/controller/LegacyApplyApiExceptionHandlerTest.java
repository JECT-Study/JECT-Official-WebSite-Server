package org.ject.support.domain.apply.controller;

import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LegacyApplyApiExceptionHandlerTest {

    private final LegacyApplyApiExceptionHandler exceptionHandler = new LegacyApplyApiExceptionHandler();

    @Test
    void recruitId가_없는_legacy_지원_API_호출은_deprecation_로그를_남긴다(CapturedOutput output) throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/apply/status");
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("recruitId", "Long");

        // when
        ErrorResponse response = exceptionHandler.handleMissingRequestParameter(exception, request);

        // then
        assertThat(response.getCode()).isEqualTo(GlobalErrorCode.MISS_REQUIRED_REQUEST_PARAMETER.getCode());
        assertThat(output)
                .contains("Deprecated legacy apply API request without recruitId")
                .contains("method=GET")
                .contains("uri=/apply/status");
    }

    @Test
    void recruitId가_아닌_파라미터_누락은_deprecation_로그를_남기지_않는다(CapturedOutput output) throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/apply/status");
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("jobFamily", "JobFamily");

        // when
        ErrorResponse response = exceptionHandler.handleMissingRequestParameter(exception, request);

        // then
        assertThat(response.getCode()).isEqualTo(GlobalErrorCode.MISS_REQUIRED_REQUEST_PARAMETER.getCode());
        assertThat(output).doesNotContain("Deprecated legacy apply API request without recruitId");
    }
}
