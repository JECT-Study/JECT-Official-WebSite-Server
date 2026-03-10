package org.ject.support.admin.apply.dto;

public record SubmittedApplyCountResponse(
        Long count
) {
    public SubmittedApplyCountResponse {
        if (count == null) {
            count = 0L;
        }
    }
}
