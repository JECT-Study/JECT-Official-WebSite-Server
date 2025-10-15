package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobFamily {
    PM("프로덕트 매니저(PM)"),
    PD("프로덕트 디자이너(PD)"),
    FE("프론트엔드 개발자(FE)"),
    BE("백엔드 개발자(BE)"),
    ;

    private final String description;
}
