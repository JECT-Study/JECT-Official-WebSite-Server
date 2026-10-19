package org.ject.support.domain.recruit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;

public record ApplyManagementRecruitSearchCondition(
        @NotNull ApplyStatus status,
        @NotNull RecruitType recruitType,
        Set<@NotNull JobFamily> jobFamilies,
        @Positive Long cursor,
        @Min(value = 1, message = "조회 개수는 1개 이상이어야 합니다.")
        @Max(value = 100, message = "조회 개수는 100개 이하여야 합니다.")
        Integer size
) {
    public int getSizeOrDefault() {
        return size == null ? 20 : size;
    }
}
