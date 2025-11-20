package org.ject.support.admin.apply.dto;

public record TempSavedApplyCountResponse(
        Long count
) {
    public TempSavedApplyCountResponse {
        if (count == null) {
            count = 0L;
        }
    }
}
