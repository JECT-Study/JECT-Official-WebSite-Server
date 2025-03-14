package org.ject.support.domain.recruit.dto;

import java.util.List;
import java.util.Map;

public record ApplyTemporaryResponse(Map<String, String> answers, List<Map<String, String>> portfolios) {

    public static ApplyTemporaryResponse of(Map<String, String> answers, List<Map<String, String>> portfolios) {
        return new ApplyTemporaryResponse(answers, portfolios);
    }
}
