package org.ject.support.domain.project.service;

import static org.ject.support.domain.project.entity.ProjectIntro.Category.DEV;
import static org.ject.support.domain.project.entity.ProjectIntro.Category.SERVICE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectIntroResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
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

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    /**
     * 주어진 기수의 프로젝트를 모두 조회합니다.
     */
    @Cacheable(value = "project", key = "#category + ':' + #semesterId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ProjectResponse> findProjects(final Project.Category category,
                                              final Long semesterId,
                                              final Pageable pageable) {
        return projectRepository.findProjectsByCategoryAndSemester(category, semesterId, pageable);
    }

    /**
     * 프로젝트 상세 정보를 조회합니다.
     */
    @Cacheable(value = "project", key = "#projectId")
    @Transactional(readOnly = true)
    public ProjectDetailResponse findProjectDetails(final Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND));

        TeamMemberNames teamMemberNames = memberRepository.findMemberNamesByTeamId(project.getTeam().getId());

        List<ProjectIntro> projectIntros = project.getProjectIntros();
        List<ProjectIntroResponse> serviceIntros = mapToResponsesByCategory(projectIntros, SERVICE);
        List<ProjectIntroResponse> devIntros = mapToResponsesByCategory(projectIntros, DEV);

        return ProjectDetailResponse.toResponse(project, teamMemberNames, serviceIntros, devIntros);
    }

    private List<ProjectIntroResponse> mapToResponsesByCategory(List<ProjectIntro> projectIntros,
                                                                final ProjectIntro.Category category) {
        return projectIntros.stream()
                .filter(projectIntro -> projectIntro.isCategory(category))
                .map(ProjectIntroResponse::toResponse)
                .toList();
    }
}
