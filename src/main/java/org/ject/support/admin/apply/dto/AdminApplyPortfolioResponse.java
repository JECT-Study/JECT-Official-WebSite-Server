package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.apply.domain.Portfolio;

public record AdminApplyPortfolioResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long portfolioId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String fileUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String fileName,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String fileSize,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String sequence
) {
    public static AdminApplyPortfolioResponse from(Portfolio portfolio) {
        return new AdminApplyPortfolioResponse(portfolio.getId(), portfolio.getFileUrl(), portfolio.getFileName(),
                String.valueOf(portfolio.getFileSize()), String.valueOf(portfolio.getSequence()));
    }
}
