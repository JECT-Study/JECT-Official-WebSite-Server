package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;

public record ApplicationFormResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, String> answers,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ApplyPortfolioDto> portfolios
) {
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
