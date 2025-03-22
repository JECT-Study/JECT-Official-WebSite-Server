package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.recruit.domain.Portfolio;

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
}
