package org.ject.support.domain.apply.dto;

import java.util.List;
import java.util.Map;

public record ApplyTemporaryRequest(Map<String, String> answers, List<ApplyPortfolioDto> portfolios) {
}
