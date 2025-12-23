package org.ject.support.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectIntroResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.dto.ProjectSummaryResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.domain.project.entity.ProjectIntro;
import org.ject.support.domain.project.exception.ProjectErrorCode;
import org.ject.support.domain.project.exception.ProjectException;
import org.ject.support.domain.project.repository.ProjectRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    /**
     * 주어진 기수의 프로젝트를 모두 조회합니다.
     */
    @Cacheable(value = "project", key = "#category + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ProjectResponse> findProjects(final Project.Category category,
                                              final Pageable pageable) {
        return projectRepository.findProjectsByCategory(category, pageable);
    }

    /**
     * 프로젝트 상세 정보를 조회합니다.
     */
    @Cacheable(value = "project", key = "#projectId")
    @Transactional(readOnly = true)
    public ProjectDetailResponse findProjectDetails(final Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND_PROJECT));

        TeamMemberNames teamMemberNames =
                memberRepository.findMemberNamesByTeamId(project.getTeam().getId());

        List<ProjectIntro> projectIntros = project.getProjectIntros();

        ProjectIntroResponse bannerImage = findSingleIntroByCategory(projectIntros, ProjectIntro.Category.BANNER);
        List<ProjectIntroResponse> sampleImages = findIntroResponsesByCategory(projectIntros, ProjectIntro.Category.SAMPLE);
        List<ProjectIntroResponse> descriptionImages = findIntroResponsesByCategory(projectIntros, ProjectIntro.Category.DESCRIPTION);

        return ProjectDetailResponse.toResponse(
                project,
                teamMemberNames,
                bannerImage,
                sampleImages,
                descriptionImages
        );
    }

    /**
     * 전체 프로젝트의 요약된 현황을 조회합니다.
     */
    @Cacheable(value = "project", key = "'summary'")
    @Transactional(readOnly = true)
    public ProjectSummaryResponse findProjectSummary() {
        List<Project> projects = projectRepository.findAll();
        return ProjectSummaryResponse.of(projects);
    }

    private ProjectIntroResponse findSingleIntroByCategory(
            List<ProjectIntro> intros,
            ProjectIntro.Category category
    ) {
        return intros.stream()
                .filter(intro -> intro.isCategory(category))
                .findFirst()
                .map(ProjectIntroResponse::toResponse)
                .orElse(null);
    }

    private List<ProjectIntroResponse> findIntroResponsesByCategory(
            List<ProjectIntro> intros,
            ProjectIntro.Category category
    ) {
        return intros.stream()
                .filter(intro -> intro.isCategory(category))
                .map(ProjectIntroResponse::toResponse)
                .toList();
    }
}
