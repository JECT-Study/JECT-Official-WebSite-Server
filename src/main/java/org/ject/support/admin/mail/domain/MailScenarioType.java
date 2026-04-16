package org.ject.support.admin.mail.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일 시나리오 발송 목적(타입)을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum MailScenarioType {

    FIRST_PASS("1차 합격"),
    FINAL_PASS("최종 합격"),
    STANDBY_PASS("예비 합격"),
    REJECT("불합격"),
    ETC("기타");

    private final String description;
}
