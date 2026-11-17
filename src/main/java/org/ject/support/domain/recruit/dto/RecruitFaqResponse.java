package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.recruit.domain.RecruitFaq;

public record RecruitFaqResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {

    // FAQ 응답 변환
    public static RecruitFaqResponse from(RecruitFaq faq) {
        return new RecruitFaqResponse(faq.title(), faq.content());
    }
}
