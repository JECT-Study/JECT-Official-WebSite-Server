package org.ject.support.external.email.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplate {
    AUTH_CODE("email-auth-template", "젝트 이메일 인증 코드 안내", "email_auth"),
    PIN_RESET("pin-reset-template", "젝트 PIN 재설정 인증 코드 안내", "pin_reset"),
    REMIND_APPLY("remind-apply-template", "젝트 지원서 접수 마감일 안내", "remind_apply");

    private final String templateName; // template file name
    private final String subject; // email subject
    private final String tag; // email tag value
}
