package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobFamily {
    PM("프로덕트 매니저(PM)", false),
    PD("프로덕트 디자이너(PD)", true),
    FE("프론트엔드 개발자(FE)", false),
    BE("백엔드 개발자(BE)", false),
    ;

    private final String description;
    private final boolean portfolioRequired;
}
