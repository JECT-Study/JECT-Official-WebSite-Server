package org.ject.support.domain.admin.dto;

public record SubmittedApplyCountResponse(
        Long count
) {
    public SubmittedApplyCountResponse {
        if (count == null) {
            count = 0L;
        }
    }
}
