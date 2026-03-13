package org.ject.support.admin.apply.dto;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;

public record AdminApplyDetailResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String careerDetails,
        String recruitType,
        String note,
        String region,
        String experiencePeriod,
        List<String> interestedDomains
) {
    public static AdminApplyDetailResponse from(Apply apply) {
        var member = apply.getMember();
        return new AdminApplyDetailResponse(
                apply.getId(),
                member.getName(),
                member.getPhoneNumber(),
                member.getEmail(),
                member.getJobFamily(),
                Optional.ofNullable(member.getCareerDetails()).map(CareerDetails::getDescription).orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getNote(),
                Optional.ofNullable(member.getRegion()).map(Region::getDescription).orElse(""),
                Optional.ofNullable(member.getExperiencePeriod()).map(ExperiencePeriod::getDescription).orElse(""),
                Optional.ofNullable(member.getInterestedDomains()).orElse(List.of())
        );
    }
}