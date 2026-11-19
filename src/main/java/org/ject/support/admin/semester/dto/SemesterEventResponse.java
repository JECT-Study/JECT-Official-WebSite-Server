package org.ject.support.admin.semester.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.recruit.domain.SemesterEvent;

public record SemesterEventResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {

    public static SemesterEventResponse from(SemesterEvent semesterEvent) {
        return new SemesterEventResponse(semesterEvent.getId(), semesterEvent.getName());
    }
}
