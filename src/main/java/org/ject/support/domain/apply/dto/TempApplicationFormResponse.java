package org.ject.support.domain.apply.dto;

import org.ject.support.domain.member.JobFamily;

import java.util.List;
import java.util.Map;

public record TempApplicationFormResponse(JobFamily jobFamily,
                                          Map<String, String> answers,
                                          List<ApplyPortfolioDto> portfolios) {

    public static TempApplicationFormResponse from(JobFamily jobFamily,
                                                   Map<String, String> answers,
                                                   List<ApplyPortfolioDto> portfolios) {
        return new TempApplicationFormResponse( jobFamily, answers, portfolios);
    }
}
