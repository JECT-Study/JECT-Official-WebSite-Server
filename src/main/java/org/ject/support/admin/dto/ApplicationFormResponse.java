package org.ject.support.admin.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;

public record ApplicationFormResponse(Map<String, String> answers,
                                      List<ApplyPortfolioDto> portfolios) {
    public ApplicationFormResponse {
        answers = Map.copyOf(
                Optional.ofNullable(answers)
                        .orElse(Map.of()));
        portfolios = List.copyOf(
                Optional.ofNullable(portfolios)
                        .orElse(List.of()));
    }
    public static ApplicationFormResponse from(final Map<String, String> answers,
                                                        final List<ApplyPortfolioDto> portfolios) {
        return new ApplicationFormResponse(answers, portfolios);
    }
}
