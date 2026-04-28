package org.ject.support.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberType {
    SEMESTER("정규 기수"),
    MAKERS("메이커스"),
    SUPPORTERS("운영 서포터즈"),
    ;

    private final String description;
}
