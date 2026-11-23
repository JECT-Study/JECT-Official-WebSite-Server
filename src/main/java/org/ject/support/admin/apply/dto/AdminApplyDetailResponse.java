package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.Question;

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
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        CareerDetails careerDetails,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String recruitType,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        SelectionResult selectionResult,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer waitlistNumber,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String note,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        Region region,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        ExperiencePeriod experiencePeriod,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> interestedDomains,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ApplyAnswerResponse> answers,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<AdminApplyPortfolioResponse> portfolios
) {
    // 공고의 전체 질문에 답변을 연결하고 지원자 정보와 포트폴리오를 조합
    public static AdminApplyDetailResponse of(Apply apply, Map<String, String> answers) {
        var applicant = apply.getApplicant();
        return new AdminApplyDetailResponse(
                apply.getId(),
                applicant.getName(),
                applicant.getPhoneNumber(),
                applicant.getEmail(),
                applicant.getJobFamily(),
                applicant.getCareerDetails(),
                apply.getRecruit().getRecruitType().name(),
                apply.getSelectionResult(),
                apply.getWaitlistNumber(),
                apply.getNote(),
                applicant.getRegion(),
                applicant.getExperiencePeriod(),
                Optional.ofNullable(applicant.getInterestedDomains()).orElse(List.of()),
                apply.getRecruit().getQuestions().stream()
                        .sorted(Comparator.comparingInt(Question::getSequence))
                        .map(question -> new ApplyAnswerResponse(question.getId(), question.getTitle(),
                                answers.get(String.valueOf(question.getId()))))
                        .toList(),
                Optional.ofNullable(apply.getApplicationForm())
                        .map(applicationForm -> applicationForm.getPortfolios())
                        .orElse(List.of())
                        .stream()
                        .map(AdminApplyPortfolioResponse::from)
                        .toList()
        );
    }
}
