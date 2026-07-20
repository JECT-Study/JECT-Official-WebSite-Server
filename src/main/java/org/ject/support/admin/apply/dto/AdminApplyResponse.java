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
        var applicant = apply.getApplicant();
        return new AdminApplyResponse(
                apply.getId(),
                applicant.getName(),
                applicant.getPhoneNumber(),
                applicant.getEmail(),
                applicant.getJobFamily(),
                Optional.ofNullable(applicant.getCareerDetails()).map(CareerDetails::getDescription).orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getNote()
        );
    }
}
