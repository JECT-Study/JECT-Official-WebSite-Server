package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.tempapply.domain.TemporaryApplication;

import java.util.List;
import java.util.Map;

public record ApplyTemporaryResponse(String jobFamily,
                                     Map<String, String> answers,
                                     List<ApplyTemporaryPortfolio> portfolios) {

    public static ApplyTemporaryResponse from(TemporaryApplication temporaryApplication) {
        return new ApplyTemporaryResponse(
                temporaryApplication.getJobFamily(),
                temporaryApplication.getAnswers(),
                temporaryApplication.getPortfolios());
    }
}
