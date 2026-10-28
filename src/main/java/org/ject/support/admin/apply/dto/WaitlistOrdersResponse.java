package org.ject.support.admin.apply.dto;

import java.util.List;

public record WaitlistOrdersResponse(
        int totalCount,
        List<WaitlistOrderResponse> content
) {
    public static WaitlistOrdersResponse from(List<WaitlistOrderResponse> content) {
        return new WaitlistOrdersResponse(content.size(), content);
    }
}
