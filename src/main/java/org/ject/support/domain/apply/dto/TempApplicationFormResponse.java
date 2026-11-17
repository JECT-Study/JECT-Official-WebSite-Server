package org.ject.support.domain.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.member.JobFamily;

import java.util.List;
import java.util.Map;

public record TempApplicationFormResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        JobFamily jobFamily,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, String> answers,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ApplyPortfolioDto> portfolios
) {

    public static TempApplicationFormResponse from(JobFamily jobFamily,
                                                   Map<String, String> answers,
                                                   List<ApplyPortfolioDto> portfolios) {
        return new TempApplicationFormResponse( jobFamily, answers, portfolios);
    }
}
