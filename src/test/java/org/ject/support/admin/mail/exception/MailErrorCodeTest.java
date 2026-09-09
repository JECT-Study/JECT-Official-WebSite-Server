package org.ject.support.admin.mail.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MailErrorCodeTest {

    @Test
    void 기존_메일_오류_코드는_호환성을_위해_유지한다() {
        assertThat(MailErrorCode.INVALID_DISPATCH_TARGET_COUNT.getCode()).isEqualTo("MAIL-15");
        assertThat(MailErrorCode.INVALID_DISPATCH_TARGETS.getCode()).isEqualTo("MAIL-16");
        assertThat(MailErrorCode.INVALID_SUBJECT.getCode()).isEqualTo("MAIL-17");
    }
}
