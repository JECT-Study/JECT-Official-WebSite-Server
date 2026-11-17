package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;

public record AdminApplyDetailResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long applyId,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        JobFamily jobFamily,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String careerDetails,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String recruitType,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        SelectionResult selectionResult,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer waitlistNumber,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String note,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String region,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String experiencePeriod,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> interestedDomains,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, String> answers,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ApplyPortfolioDto> portfolios
) {
    public static AdminApplyDetailResponse from(Apply apply, Map<String, String> answers) {
        var applicant = apply.getApplicant();
        return new AdminApplyDetailResponse(
                apply.getId(),
                applicant.getName(),
                applicant.getPhoneNumber(),
                applicant.getEmail(),
                applicant.getJobFamily(),
                Optional.ofNullable(applicant.getCareerDetails()).map(CareerDetails::getDescription).orElse(""),
                apply.getRecruit().getRecruitType().name(),
                apply.getSelectionResult(),
                apply.getWaitlistNumber(),
                apply.getNote(),
                Optional.ofNullable(applicant.getRegion()).map(Region::getDescription).orElse(""),
                Optional.ofNullable(applicant.getExperiencePeriod()).map(ExperiencePeriod::getDescription).orElse(""),
                Optional.ofNullable(applicant.getInterestedDomains()).orElse(List.of()),
                answers,
                Optional.ofNullable(apply.getApplicationForm())
                        .map(applicationForm -> applicationForm.getPortfolios())
                        .orElse(List.of())
                        .stream()
                        .map(ApplyPortfolioDto::from)
                        .toList()
        );
    }
}
