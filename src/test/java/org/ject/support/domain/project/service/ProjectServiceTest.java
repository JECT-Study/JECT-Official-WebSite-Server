package org.ject.support.domain.project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.project.entity.ProjectIntro.Category;
import static org.ject.support.domain.project.entity.ProjectIntro.builder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

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
                .semester("1기")
                .summary("summary")
                .techStack("java,Spring, JPA, QueryDSL,MySQL,AWS")
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
    @DisplayName("프로젝트 상세 조회")
    void find_project_details() {
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
    @DisplayName("프로젝트 상세 조회 시 서비스 소개서는 sequence 기준 오름차순 정렬되어야 함")
    void find_project_details_sort_project_intro_by_sequence_ascending() {
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
    @DisplayName("존재하지 않는 프로젝트 상세 조회 시 예외 발생")
    void find_project_details_fail_by_not_found() {
        // given
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> projectService.findProjectDetails(1L))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    @DisplayName("프로젝트 상세 조회 시 기술 스택을 배열 형태로 반환")
    void get_tech_stack_for_list() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(project));
        when(memberRepository.findMemberNamesByTeamId(1L)).thenReturn(
                new TeamMemberNames(projectManagers, productDesigners, frontendDevelopers, backendDevelopers));

        // when
        ProjectDetailResponse result = projectService.findProjectDetails(1L);

        // then
        assertThat(result.techStack()).isExactlyInstanceOf(ArrayList.class);
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