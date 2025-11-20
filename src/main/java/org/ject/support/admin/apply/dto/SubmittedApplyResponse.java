package org.ject.support.admin.apply.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.JobFamily;

public record SubmittedApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
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
                Optional.ofNullable(apply.getApplicationForm())
                        .map(ApplicationForm::getPortfolios)
                        .map(portfolioList -> !portfolioList.isEmpty())
                        .orElse(false),
                ApplicationFormResponse.from(answers, portfolios)
        );
    }
}
