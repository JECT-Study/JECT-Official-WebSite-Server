package org.ject.support.domain.applicant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.Region;

import java.util.List;

@Builder
public record ApplicantProfileResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        CareerDetails careerDetails,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        Region region,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        ExperiencePeriod experiencePeriod,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
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
