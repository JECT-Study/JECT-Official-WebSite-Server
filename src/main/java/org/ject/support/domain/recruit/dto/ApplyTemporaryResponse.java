package org.ject.support.domain.recruit.dto;

import java.util.List;
import java.util.Map;

public record ApplyTemporaryResponse(Map<String, String> answers, List<ApplyTemporaryPortfolio> portfolios) {

    public static ApplyTemporaryResponse of(Map<String, String> answers, List<ApplyTemporaryPortfolio> portfolios) {
        return new ApplyTemporaryResponse(answers, portfolios);
    }
}
