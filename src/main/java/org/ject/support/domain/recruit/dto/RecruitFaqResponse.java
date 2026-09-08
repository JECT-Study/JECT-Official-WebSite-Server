package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.recruit.domain.RecruitFaq;

public record RecruitFaqResponse(
        String title,
        String content
) {

    // FAQ 응답 변환
    public static RecruitFaqResponse from(RecruitFaq faq) {
        return new RecruitFaqResponse(faq.title(), faq.content());
    }
}
