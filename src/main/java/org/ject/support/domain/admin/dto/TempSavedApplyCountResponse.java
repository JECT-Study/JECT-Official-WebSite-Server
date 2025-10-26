package org.ject.support.domain.admin.dto;

public record TempSavedApplyCountResponse(
        Long count
) {
    public TempSavedApplyCountResponse {
        if (count == null) {
            count = 0L;
        }
    }
}
