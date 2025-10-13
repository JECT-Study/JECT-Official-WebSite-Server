package org.ject.support.domain.apply.dto;

import java.util.List;
import java.util.Map;

public record TempApplicationFormResponse(Map<String, String> answers,
                                          List<ApplyPortfolioDto> portfolios) {

    public static TempApplicationFormResponse from(Map<String, String> answers, List<ApplyPortfolioDto> portfolios) {
        return new TempApplicationFormResponse(answers, portfolios);
    }
}
