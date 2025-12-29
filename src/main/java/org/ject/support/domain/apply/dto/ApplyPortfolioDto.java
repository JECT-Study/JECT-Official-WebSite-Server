package org.ject.support.domain.apply.dto;

import org.ject.support.domain.apply.domain.Portfolio;

public record ApplyPortfolioDto(String fileUrl,
                                String fileName,
                                String fileSize,
                                String sequence) {

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
