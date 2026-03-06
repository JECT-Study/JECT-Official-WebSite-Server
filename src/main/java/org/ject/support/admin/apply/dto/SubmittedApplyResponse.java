package org.ject.support.admin.apply.dto;

import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SubmittedApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String region,
        String careerDetails,
        String experiencePeriod,
        List<String> interestedDomains,
        boolean hasPortfolio,
        ApplicationFormResponse applicationFormResponse
) {
    public static SubmittedApplyResponse from(Apply apply,
                                                        Map<String, String> answers,
                                                        List<ApplyPortfolioDto> portfolios) {
        return new SubmittedApplyResponse(
                apply.getId(),
                apply.getMember().getName(),
                apply.getMember().getPhoneNumber(),
                apply.getMember().getEmail(),
                apply.getMember().getJobFamily(),
                Optional.ofNullable(apply.getMember().getRegion())
                        .map(Region::getDescription)
                        .orElse(""),
                Optional.ofNullable(apply.getMember().getCareerDetails())
                        .map(CareerDetails::getDescription)
                        .orElse(""),
                Optional.ofNullable(apply.getMember().getExperiencePeriod())
                        .map(ExperiencePeriod::getDescription)
                        .orElse(""),
                new ArrayList<>(Optional.ofNullable(apply.getMember().getInterestedDomains())
                        .orElse(List.of())
                ),
                Optional.ofNullable(apply.getApplicationForm())
                        .map(ApplicationForm::getPortfolios)
                        .map(portfolioList -> !portfolioList.isEmpty())
                        .orElse(false),
                ApplicationFormResponse.from(answers, portfolios)
        );
    }
}
