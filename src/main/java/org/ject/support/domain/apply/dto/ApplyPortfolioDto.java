package org.ject.support.domain.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.apply.domain.Portfolio;

public record ApplyPortfolioDto(
        @Schema(nullable = false)
        String fileUrl,
        @Schema(nullable = false)
        String fileName,
        @Schema(nullable = false)
        String fileSize,
        @Schema(nullable = false)
        String sequence
) {

    public Portfolio toEntity() {
        return Portfolio.builder()
                .fileUrl(fileUrl)
                .fileName(fileName)
                .fileSize(Long.parseLong(fileSize))
                .sequence(Integer.parseInt(sequence))
                .build();
    }

    public static ApplyPortfolioDto from(Portfolio portfolio) {
        return new ApplyPortfolioDto(
                portfolio.getFileUrl(),
                portfolio.getFileName(),
                String.valueOf(portfolio.getFileSize()),
                String.valueOf(portfolio.getSequence())
        );
    }
}
