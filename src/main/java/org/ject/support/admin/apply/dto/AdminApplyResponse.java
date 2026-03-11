package org.ject.support.admin.apply.dto;

import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;

public record AdminApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String careerDetails,
        String recruitType,
        String note
) {
    public static AdminApplyResponse from(Apply apply) {
        return new AdminApplyResponse(
                apply.getId(),
                apply.getMember().getName(),
                apply.getMember().getPhoneNumber(),
                apply.getMember().getEmail(),
                apply.getMember().getJobFamily(),
                Optional.ofNullable(apply.getMember().getCareerDetails()).map(CareerDetails::getDescription).orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getNote()
        );
    }
}
