package org.ject.support.domain.recruit.dto;

import java.util.List;
import java.util.Map;

public record ApplyTemporaryRequest(Map<String, String> answers, List<Map<String, String>> portfolios) {
}
