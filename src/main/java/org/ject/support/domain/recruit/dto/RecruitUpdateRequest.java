package org.ject.support.domain.recruit.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitFaq;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitUpdateRequest(
        @NotNull(message = "직군은 필수입니다.")
        JobFamily jobFamily,

        @NotNull(message = "모집 시작일은 필수입니다.")
        LocalDateTime startDate,

        @NotNull(message = "모집 종료일은 필수입니다.")
        @Future(message = "모집 종료일은 현재 시각보다 이후여야 합니다.")
        LocalDateTime endDate,

        @Size(max = 100000, message = "모집 정보는 100,000자 이하여야 합니다.")
        String recruitInformation,

        @Size(max = 100000, message = "안내사항은 100,000자 이하여야 합니다.")
        String notice,

        List<@Valid RecruitFaqRequest> faqs
) {
    public RecruitUpdateRequest(JobFamily jobFamily, LocalDateTime startDate, LocalDateTime endDate) {
        this(jobFamily, startDate, endDate, null, null, List.of());
    }

    // FAQ 요청 목록을 도메인 값으로 변환
    public List<RecruitFaq> getFaqsOrNull() {
        return faqs == null ? null : RecruitFaqRequest.toDomains(faqs);
    }
}
