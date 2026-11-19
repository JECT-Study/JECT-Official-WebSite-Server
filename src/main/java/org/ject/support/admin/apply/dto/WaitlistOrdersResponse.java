package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record WaitlistOrdersResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int totalCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<WaitlistOrderResponse> content
) {
    public static WaitlistOrdersResponse from(List<WaitlistOrderResponse> content) {
        return new WaitlistOrdersResponse(content.size(), content);
    }
}
