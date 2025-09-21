package org.ject.support.domain.apply.dto;

import org.ject.support.domain.apply.dto.ApplyPortfolioDto;

import java.util.List;
import java.util.Map;

public record SubmitApplicationRequest(Map<String, String> answers, List<ApplyPortfolioDto> portfolios) {
}
