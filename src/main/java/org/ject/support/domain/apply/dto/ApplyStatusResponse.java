package org.ject.support.domain.apply.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.ject.support.domain.apply.domain.Apply.Status;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplyStatusResponse(
        Status status,
        String step
) {

    public static ApplyStatusResponse of(Status status) {
        return new ApplyStatusResponse(status, null);
    }

    // 프로필 작성을 하지 않았을 경우
    public static ApplyStatusResponse tempSavedProfile() {
        return new ApplyStatusResponse(Status.TEMP_SAVED, "PROFILE");
    }

    // 프로필 작성 이후, 지원서 작성 중인 경우
    public static ApplyStatusResponse tempSavedApply() {
        return new ApplyStatusResponse(Status.TEMP_SAVED, "APPLY");
    }
}
