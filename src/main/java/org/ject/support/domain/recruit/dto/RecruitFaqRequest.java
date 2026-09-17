package org.ject.support.domain.recruit.dto;

import java.util.List;
import org.ject.support.domain.recruit.domain.RecruitFaq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruitFaqRequest(
        @NotBlank(message = "FAQ 제목을 입력해주세요.")
        @Size(max = 200, message = "FAQ 제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "FAQ 내용을 입력해주세요.")
        @Size(max = 100000, message = "FAQ 내용은 100,000자 이하여야 합니다.")
        String content
) {

    // FAQ 요청 목록을 도메인 값으로 변환
    public static List<RecruitFaq> toDomains(List<RecruitFaqRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> RecruitFaq.create(request.title(), request.content()))
                .toList();
    }
}
