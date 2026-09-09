package org.ject.support.domain.recruit.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitRegisterRequest(
        @NotNull(message = "직군은 필수입니다.")
        JobFamily jobFamily,

        @NotNull(message = "모집 시작일은 필수입니다.")
        LocalDateTime startDate,

        @NotNull(message = "모집 종료일은 필수입니다.")
        @Future(message = "모집 종료일은 현재 시각보다 이후여야 합니다.")
        LocalDateTime endDate,

        @Size(max = 500, message = "모집공고 요약은 500자 이하로 입력해주세요.")
        String summary,

        @Size(max = 100000, message = "모집 정보는 100,000자 이하여야 합니다.")
        String recruitInformation,

        @Size(max = 100000, message = "안내사항은 100,000자 이하여야 합니다.")
        String notice,

        List<@Valid RecruitFaqRequest> faqs
) {
    public RecruitRegisterRequest(JobFamily jobFamily, LocalDateTime startDate, LocalDateTime endDate) {
        this(jobFamily, startDate, endDate, null, null, null, List.of());
    }

    public Recruit toEntity(Semester semester) {
        return Recruit.builder()
                .semester(semester)
                .jobFamily(this.jobFamily)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .summary(this.summary)
                .recruitInformation(this.recruitInformation)
                .notice(this.notice)
                .faqs(RecruitFaqRequest.toDomains(this.faqs))
                .build();
    }
}
