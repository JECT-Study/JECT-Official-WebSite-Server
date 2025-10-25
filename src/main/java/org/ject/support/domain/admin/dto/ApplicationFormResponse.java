package org.ject.support.domain.admin.dto;

import java.util.List;
import java.util.Map;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;

public record ApplicationFormResponse(Map<String, String> answers,
                                      List<ApplyPortfolioDto> portfolios) {
    public static ApplicationFormResponse from(Map<String, String> answers, List<ApplyPortfolioDto> portfolios) {
        return new ApplicationFormResponse(answers, portfolios);
    }
}
