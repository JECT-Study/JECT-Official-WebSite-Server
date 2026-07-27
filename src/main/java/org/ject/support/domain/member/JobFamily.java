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
    APP("앱 개발자(APP)", false),
    SUPPORTER("운영 서포터즈", false),
    OPS("운영팀(OPS)", false),
    INFRA("인프라팀(INFRA)", false),
    BX("BX팀(BX)", false),
    ER("대외협력팀(ER)", false),
    ;

    private final String description;
    private final boolean portfolioRequired;

    public boolean isAvailableFor(MemberType memberType) {
        return switch (memberType) {
            case SEMESTER -> this == PM || this == PD || this == FE || this == BE || this == APP;
            case MAKERS -> this == PM || this == PD || this == FE || this == BE;
            case SUPPORTERS -> this == OPS || this == INFRA || this == BX || this == ER;
        };
    }
}
