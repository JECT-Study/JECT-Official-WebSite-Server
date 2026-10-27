package org.ject.support.external.email.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailErrorCodeTest {

    @ParameterizedTest
    @CsvSource({
            "INVALID_EMAIL_TEMPLATE, EMAIL-1, 400",
            "EMAIL_SEND_FAILURE, EMAIL-2, 503",
            "EMAIL_SEND_RESULT_UNKNOWN, EMAIL-3, 503",
            "TOO_MANY_EMAIL_REQUESTS, EMAIL-4, 429"
    })
    void 이메일_오류는_번호_코드와_기존_HTTP_상태를_반환한다(String name, String code, int status) {
        // given
        EmailErrorCode errorCode = EmailErrorCode.valueOf(name);

        // when, then
        assertThat(errorCode.getCode()).isEqualTo(code);
        assertThat(errorCode.getHttpStatus().value()).isEqualTo(status);
        assertThat(EmailErrorCode.normalizeCode(name)).isEqualTo(code);
        assertThat(EmailErrorCode.normalizeCode(code)).isEqualTo(code);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"MAIL-21", "UNKNOWN_LEGACY_CODE"})
    void 다른_도메인과_알_수_없는_코드와_정보_부재는_변경하지_않는다(String code) {
        assertThat(EmailErrorCode.normalizeCode(code)).isEqualTo(code);
    }
}
