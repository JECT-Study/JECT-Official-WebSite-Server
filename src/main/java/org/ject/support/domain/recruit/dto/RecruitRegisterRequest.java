package org.ject.support.domain.recruit.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitRegisterRequest(
        @NotBlank(message = "모집공고 제목은 필수입니다.")
        @Size(max = 100, message = "모집공고 제목은 100자 이하로 입력해주세요.")
        String title,

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

        List<@Valid RecruitFaqRequest> faqs,

        @NotNull RecruitType recruitType,
        @NotNull RecruitTypeDetail recruitTypeDetail,
        Long semesterId
) {
    public Recruit toEntity(Semester semester) {
        return Recruit.create(semester, title, recruitType, recruitTypeDetail, jobFamily,
                startDate, endDate, summary, recruitInformation, notice, RecruitFaqRequest.toDomains(faqs));
    }
}
