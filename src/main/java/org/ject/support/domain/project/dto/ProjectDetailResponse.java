package org.ject.support.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.project.entity.Project;

@Builder
public record ProjectDetailResponse(
        String thumbnailUrl,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        TeamMemberNames teamMemberNames,
        List<String> techStack,
        String description,
        String serviceUrl,
        List<ProjectIntroResponse> serviceIntros,
        List<ProjectIntroResponse> devIntros
) {

    @QueryProjection
    public ProjectDetailResponse {
    }

    public static ProjectDetailResponse toResponse(Project project,
                                                   TeamMemberNames teamMemberNames,
                                                   List<ProjectIntroResponse> serviceIntros,
                                                   List<ProjectIntroResponse> devIntros) {
        return ProjectDetailResponse.builder()
                .thumbnailUrl(project.getThumbnailUrl())
                .name(project.getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .teamMemberNames(teamMemberNames)
                .techStack(project.getTechStack())
                .description(project.getDescription())
                .serviceUrl(project.getServiceUrl())
                .serviceIntros(serviceIntros)
                .devIntros(devIntros)
                .build();
    }
}
