package org.ject.support.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.project.entity.ProjectIntro.Category;
import static org.ject.support.domain.project.entity.ProjectIntro.builder;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectIntroResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.domain.project.entity.ProjectIntro;
import org.ject.support.domain.project.exception.ProjectException;
import org.ject.support.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

class ProjectServiceTest extends UnitTestSupport {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    private Project project;
    private List<String> projectManagers;
    private List<String> productDesigners;
    private List<String> frontendDevelopers;
    private List<String> backendDevelopers;

    @BeforeEach
    void setUp() {
        projectManagers = List.of();
        productDesigners = List.of("designer1");
        frontendDevelopers = List.of("front1", "front2");
        backendDevelopers = List.of("back1", "back2", "back3");
        ProjectIntro serviceIntro1 = createProjectIntro(1L, "serviceImage1.png", Category.SERVICE, 1);
        ProjectIntro serviceIntro2 = createProjectIntro(2L, "serviceImage2.png", Category.SERVICE, 2);
        ProjectIntro serviceIntro3 = createProjectIntro(3L, "serviceImage3.png", Category.SERVICE, 3);
        ProjectIntro devIntro1 = createProjectIntro(4L, "devImage1.png", Category.DEV, 1);
        project = Project.builder()
                .id(1L)
                .semesterId(1L)
                .summary("summary")
                .techStack(List.of("java", "Spring", "JPA", "QueryDSL", "MySQL", "AWS"))
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 6, 30))
                .description("description")
                .thumbnailUrl("thumbnail.png")
                .serviceUrl("service.com")
                .team(Team.builder().id(1L).name("team").build())
                .projectIntros(List.of(serviceIntro1, serviceIntro2, serviceIntro3, devIntro1))
                .build();
    }

    @Test
    void 프로젝트_상세_조회() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(project));
        when(memberRepository.findMemberNamesByTeamId(1L)).thenReturn(
                new TeamMemberNames(projectManagers, productDesigners, frontendDevelopers, backendDevelopers));

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.thumbnailUrl()).isEqualTo(project.getThumbnailUrl());
        assertThat(result.name()).isEqualTo(project.getName());
        assertThat(result.startDate()).isEqualTo(project.getStartDate());
        assertThat(result.endDate()).isEqualTo(project.getEndDate());
        assertThat(result.teamMemberNames().productManagers()).hasSize(0);
        assertThat(result.teamMemberNames().productDesigners()).hasSize(1);
        assertThat(result.teamMemberNames().frontendDevelopers()).hasSize(2);
        assertThat(result.teamMemberNames().backendDevelopers()).hasSize(3);
        assertThat(result.description()).isEqualTo(project.getDescription());
        assertThat(result.serviceUrl()).isEqualTo(project.getServiceUrl());
        assertThat(result.serviceIntros()).hasSize(3);
        assertThat(result.devIntros()).hasSize(1);
    }

    @Test
    void 프로젝트_상세_조회_시_서비스_소개서는_sequence_기준으로_오름차순_정렬() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(project));
        when(memberRepository.findMemberNamesByTeamId(1L)).thenReturn(
                new TeamMemberNames(projectManagers, productDesigners, frontendDevelopers, backendDevelopers));

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.serviceIntros())
                .extracting(ProjectIntroResponse::sequence)
                .containsExactly(1, 2, 3);
    }

    @Test
    void 존재하지_않는_프로젝트_상세_조회_시_예외_발생() {
        // given
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> projectService.findProjectDetails(1L))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    void 프로젝트_상세_조회_시_기술_스택을_배열_형태로_반환() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(project));
        when(memberRepository.findMemberNamesByTeamId(1L)).thenReturn(
                new TeamMemberNames(projectManagers, productDesigners, frontendDevelopers, backendDevelopers));

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.techStack()).containsExactly("java", "Spring", "JPA", "QueryDSL", "MySQL", "AWS");
    }

    private ProjectIntro createProjectIntro(Long id, String imageUrl, Category category, int sequence) {
        return builder()
                .id(id)
                .imageUrl(imageUrl)
                .category(category)
                .sequence(sequence)
                .build();
    }
}
