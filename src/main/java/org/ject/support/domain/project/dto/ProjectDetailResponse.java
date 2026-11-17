package org.ject.support.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProjectDetailResponse(
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startDate,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate endDate,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        TeamMemberNames teamMemberNames,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> techStack,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> badges,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String serviceUrl,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String githubUrl,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        ProjectIntroResponse bannerImageUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ProjectIntroResponse> sampleImageUrls,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ProjectIntroResponse> descriptionImageUrls
) {

    @QueryProjection
    public ProjectDetailResponse {
    }

    public static ProjectDetailResponse toResponse(Project project,
                                                   TeamMemberNames teamMemberNames,
                                                   ProjectIntroResponse bannerImageUrl,
                                                   List<ProjectIntroResponse> sampleImageUrls,
                                                   List<ProjectIntroResponse> descriptionImageUrls) {
        return ProjectDetailResponse.builder()
                .name(project.getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .teamMemberNames(teamMemberNames)
                .techStack(project.getTechStack())
                .badges(project.getBadges())
                .description(project.getDescription())
                .serviceUrl(project.getServiceUrl())
                .githubUrl(project.getGithubUrl())
                .bannerImageUrl(bannerImageUrl)
                .sampleImageUrls(sampleImageUrls)
                .descriptionImageUrls(descriptionImageUrls)
                .build();
    }
}
