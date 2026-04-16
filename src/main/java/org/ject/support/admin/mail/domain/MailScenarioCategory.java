package org.ject.support.admin.mail.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일 시나리오의 대분류(소속)를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum MailScenarioCategory {

    GENERAL("공통"),
    CLUB_MEMBER("동아리원"),
    MAKERS("메이커스"),
    SUPPORTERS("운영 서포터즈");

    private final String description;
}
