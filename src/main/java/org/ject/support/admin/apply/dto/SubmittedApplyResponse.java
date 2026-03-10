package org.ject.support.admin.apply.dto;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;

import java.util.Optional;

public record SubmittedApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String careerDetails,
        String recruitType,
        String note
) {
    public static SubmittedApplyResponse from(Apply apply) {
        return new SubmittedApplyResponse(
                apply.getId(),
                apply.getMember().getName(),
                apply.getMember().getPhoneNumber(),
                apply.getMember().getEmail(),
                apply.getMember().getJobFamily(),
                Optional.ofNullable(apply.getMember().getCareerDetails())
                        .map(CareerDetails::getDescription)
                        .orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getNote()
        );
    }
}
