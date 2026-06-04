package org.ject.support.domain.applicant.dto;

import lombok.Builder;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.Region;

import java.util.List;

@Builder
public record ApplicantProfileResponse(
        Long id,
        String name,
        String phoneNumber,
        CareerDetails careerDetails,
        Region region,
        ExperiencePeriod experiencePeriod,
        List<String> interestedDomains
) {
    public static ApplicantProfileResponse of(Applicant applicant) {
        return ApplicantProfileResponse.builder()
                .id(applicant.getId())
                .name(applicant.getName())
                .phoneNumber(applicant.getPhoneNumber())
                .careerDetails(applicant.getCareerDetails())
                .region(applicant.getRegion())
                .experiencePeriod(applicant.getExperiencePeriod())
                .interestedDomains(applicant.getInterestedDomains())
                .build();
    }
}
