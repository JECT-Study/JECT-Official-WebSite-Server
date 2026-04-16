package org.ject.support.admin.mail.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일 템플릿의 커스텀 변수에 적용할 수 있는 입력 타입입니다.
 */
@Getter
@RequiredArgsConstructor
public enum VariableInputType {
    TEXT("텍스트"),
    URL("URL 링크"),
    EMAIL("이메일 주소"),
    PHONE("전화번호");

    private final String description;
}
