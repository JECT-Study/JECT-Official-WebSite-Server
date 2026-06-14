package org.ject.support.domain.project.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectIntroResponse;
import org.ject.support.domain.project.dto.ProjectSummaryResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.domain.project.entity.ProjectIntro;
import org.ject.support.domain.project.exception.ProjectException;
import org.ject.support.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.project.entity.ProjectIntro.Category;
import static org.mockito.Mockito.when;

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
        ProjectIntro serviceIntro1 = createProjectIntro(1L, "serviceImage1.png", Category.SAMPLE, 1);
        ProjectIntro serviceIntro2 = createProjectIntro(2L, "serviceImage2.png", Category.SAMPLE, 2);
        ProjectIntro serviceIntro3 = createProjectIntro(3L, "serviceImage3.png", Category.SAMPLE, 3);
        ProjectIntro devIntro1 = createProjectIntro(4L, "devImage1.png", Category.DESCRIPTION, 1);
        project = Project.builder()
                .id(1L)
                .summary("summary")
                .techStack(List.of("java", "Spring", "JPA", "QueryDSL", "MySQL", "AWS"))
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 6, 30))
                .description("description")
                .thumbnailUrl("thumbnail.png")
                .serviceUrl("service.com")
                .team(Team.builder().id(1L).name("team").semesterId(1L).build())
                .projectIntros(List.of(serviceIntro1, serviceIntro2, serviceIntro3, devIntro1))
                .build();
    }

    @Test
    void 프로젝트_상세_조회() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(project));
        //TODO: team_id 기반으로 member와 activity를 조회해도록 구현 후 검증 추가

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.name()).isEqualTo(project.getName());
        assertThat(result.startDate()).isEqualTo(project.getStartDate());
        assertThat(result.endDate()).isEqualTo(project.getEndDate());
        assertThat(result.description()).isEqualTo(project.getDescription());
        assertThat(result.serviceUrl()).isEqualTo(project.getServiceUrl());
        assertThat(result.sampleImageUrls()).hasSize(3);
        assertThat(result.descriptionImageUrls()).hasSize(1);
    }

    @Test
    void 프로젝트_상세_조회_시_서비스_소개서는_sequence_기준으로_오름차순_정렬() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(project));
        //TODO: team_id 기반으로 member와 activity를 조회해도록 구현 후 검증 추가

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.sampleImageUrls())
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
        //TODO: team_id 기반으로 member와 activity를 조회해도록 구현 후 검증 추가

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.techStack()).containsExactly("java", "Spring", "JPA", "QueryDSL", "MySQL", "AWS");
    }

    @Test
    void 전체_프로젝트_요약_조회() {
        Project p1 = Project.builder().id(10L).category(Project.Category.SEMESTER_1).build();
        Project p2 = Project.builder().id(11L).category(Project.Category.SEMESTER_2).build();
        Project p3 = Project.builder().id(12L).category(Project.Category.SEMESTER_2).build();

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        // when
        ProjectSummaryResponse summary = projectService.findProjectSummary();

        // then
        assertThat(summary.categorySummaries()).hasSize(1 + Project.Category.values().length);
        assertThat(summary.categorySummaries().get(0).categoryName()).isEqualTo("ALL");
        assertThat(summary.categorySummaries().get(0).count()).isEqualTo(3L);

        assertThat(summary.categorySummaries().get(1).categoryName()).isEqualTo(Project.Category.SEMESTER_1.name());
        assertThat(summary.categorySummaries().get(1).count()).isEqualTo(1L);

        assertThat(summary.categorySummaries().get(2).categoryName()).isEqualTo(Project.Category.SEMESTER_2.name());
        assertThat(summary.categorySummaries().get(2).count()).isEqualTo(2L);

        assertThat(summary.categorySummaries().get(3).categoryName()).isEqualTo(Project.Category.SEMESTER_3.name());
        assertThat(summary.categorySummaries().get(3).count()).isEqualTo(0L);
    }

    private ProjectIntro createProjectIntro(Long id, String imageUrl, Category category, int sequence) {
        return ProjectIntro.builder()
                .id(id)
                .imageUrl(imageUrl)
                .category(category)
                .sequence(sequence)
                .build();
    }
}
