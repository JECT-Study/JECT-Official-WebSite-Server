package org.ject.support.admin.apply.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.common.util.DateTimeUtil;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.JobFamily;

public record SubmittedApplyDetailResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String careerDetails,
        String recruitType,
        String note,
        String region,
        String experiencePeriod,
        List<String> interestedDomains,
        String createdAt,
        String updatedAt
        // ApplicationFormResponse applicationFormResponse
) {
    public static SubmittedApplyDetailResponse from(Apply apply,
                                                    Map<String, String> answers,
                                                    List<ApplyPortfolioDto> portfolios) {
        return new SubmittedApplyDetailResponse(
                apply.getId(),
                apply.getMember().getName(),
                apply.getMember().getPhoneNumber(),
                apply.getMember().getEmail(),
                apply.getMember().getJobFamily(),
                Optional.ofNullable(apply.getMember().getCareerDetails())
                        .map(org.ject.support.domain.member.CareerDetails::getDescription)
                        .orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getNote(),
                Optional.ofNullable(apply.getMember().getRegion())
                        .map(org.ject.support.domain.member.Region::getDescription)
                        .orElse(""),
                Optional.ofNullable(apply.getMember().getExperiencePeriod())
                        .map(org.ject.support.domain.member.ExperiencePeriod::getDescription)
                        .orElse(""),
                apply.getMember().getInterestedDomains() == null ? List.of() : apply.getMember().getInterestedDomains(),
                DateTimeUtil.formatWithDayOfWeek(apply.getCreatedAt()),
                DateTimeUtil.formatWithDayOfWeek(apply.getUpdatedAt())
                // ApplicationFormResponse.from(answers, portfolios)
        );
    }
}
