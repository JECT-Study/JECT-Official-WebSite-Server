package org.ject.support.admin.apply.dto;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;

public record AdminApplyDetailResponse(
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
        List<ApplyPortfolioDto> portfolios
) {
    public static AdminApplyDetailResponse from(Apply apply) {
        var applicant = apply.getApplicant();
        return new AdminApplyDetailResponse(
                apply.getId(),
                applicant.getName(),
                applicant.getPhoneNumber(),
                applicant.getEmail(),
                applicant.getJobFamily(),
                Optional.ofNullable(applicant.getCareerDetails()).map(CareerDetails::getDescription).orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getNote(),
                Optional.ofNullable(applicant.getRegion()).map(Region::getDescription).orElse(""),
                Optional.ofNullable(applicant.getExperiencePeriod()).map(ExperiencePeriod::getDescription).orElse(""),
                Optional.ofNullable(applicant.getInterestedDomains()).orElse(List.of()),
                Optional.ofNullable(apply.getApplicationForm())
                        .map(applicationForm -> applicationForm.getPortfolios())
                        .orElse(List.of())
                        .stream()
                        .map(ApplyPortfolioDto::from)
                        .toList()
        );
    }
}
