package org.ject.support.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProjectDetailResponse(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        TeamMemberNames teamMemberNames,
        List<String> techStack,
        List<String> badges,
        String description,
        String serviceUrl,
        String githubUrl,
        ProjectIntroResponse bannerImageUrl,
        List<ProjectIntroResponse> sampleImageUrls,
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
