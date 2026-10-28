package org.ject.support.domain.recruit.dto;

import java.util.List;
import org.ject.support.common.response.CursorPageResponse;

public record ApplyManagementRecruitsResponse(
        List<RecruitTypeCountResponse> recruitTypeCounts,
        CursorPageResponse<ApplyManagementRecruitResponse> recruits
) {
    public static ApplyManagementRecruitsResponse of(
            List<RecruitTypeCountResponse> recruitTypeCounts,
            CursorPageResponse<ApplyManagementRecruitResponse> recruits
    ) {
        return new ApplyManagementRecruitsResponse(recruitTypeCounts, recruits);
    }
}
