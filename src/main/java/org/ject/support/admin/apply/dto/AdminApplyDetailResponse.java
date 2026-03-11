package org.ject.support.admin.apply.dto;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.Region;

public record AdminApplyDetailResponse(
        AdminApplyResponse applyResponse,
        String region,
        String experiencePeriod,
        List<String> interestedDomains
) {
    public static AdminApplyDetailResponse from(Apply apply) {
        var appliedMember = apply.getMember();
        return new AdminApplyDetailResponse(
                AdminApplyResponse.from(apply),
                Optional.ofNullable(appliedMember.getRegion()).map(Region::getDescription).orElse(""),
                Optional.ofNullable(appliedMember.getExperiencePeriod()).map(ExperiencePeriod::getDescription).orElse(""),
                Optional.ofNullable(appliedMember.getInterestedDomains()).orElse(List.of())
        );
    }
}
