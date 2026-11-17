package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.recruit.domain.Semester;

public record SemesterResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {
    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
                semester.getId(), 
                semester.getName()
        );
    }
}
