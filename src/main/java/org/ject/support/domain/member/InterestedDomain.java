package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterestedDomain {
    GAME("게임"),
    EDUCATION("교육"),
    MARKETING("마케팅"),
    MOBILITY("모빌리티"),
    PRODUCTIVITY("생산성"),
    SOCIAL_NETWORK("소셜 네트워크"),
    UTILITY("유틸리티"),
    E_COMMERCE("이커머스"),
    COMMUNITY("커뮤니티"),
    CONTENTS("콘텐츠"),
    TRAVELTECH("트래블테크"),
    FASHION_BEAUTY("패션/뷰티"),
    FOODTECH("푸드테크"),
    PROPTECH("프롭테크"),
    FINTECH("핀테크"),
    HEALTHCARE("헬스케어"),
    HR("HR"),
    ;

    private final String description;

}
