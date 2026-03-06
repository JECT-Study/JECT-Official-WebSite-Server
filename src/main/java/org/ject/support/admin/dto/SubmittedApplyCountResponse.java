package org.ject.support.admin.dto;

public record SubmittedApplyCountResponse(
        Long count
) {
    public SubmittedApplyCountResponse {
        if (count == null) {
            count = 0L;
        }
    }
}
