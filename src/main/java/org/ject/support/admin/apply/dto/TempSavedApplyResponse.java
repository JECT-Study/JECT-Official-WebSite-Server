package org.ject.support.admin.apply.dto;

import org.ject.support.domain.admin.dto.ApplicationFormResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.JobFamily;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TempSavedApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        boolean hasPortfolio,
        ApplicationFormResponse applicationFormResponse
) {
    public static TempSavedApplyResponse from(Apply apply,
                                              Map<String, String> answers,
                                              List<ApplyPortfolioDto> portfolios) {
        return new TempSavedApplyResponse(
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
