package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.ject.support.common.response.CursorPageResponse;

public record ApplyManagementRecruitsResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<RecruitTypeCountResponse> recruitTypeCounts,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        CursorPageResponse<ApplyManagementRecruitResponse> recruits
) {
    public static ApplyManagementRecruitsResponse of(
            List<RecruitTypeCountResponse> recruitTypeCounts,
            CursorPageResponse<ApplyManagementRecruitResponse> recruits
    ) {
        return new ApplyManagementRecruitsResponse(recruitTypeCounts, recruits);
    }
}
