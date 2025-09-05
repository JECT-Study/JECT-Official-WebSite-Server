package org.ject.support.domain.project.repository;

import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.repository.TeamRepository;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.project.entity.Project.Category.HACKATHON;
import static org.ject.support.domain.project.entity.Project.Category.MAIN;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ProjectQueryRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamRepository teamRepository;

    private Team team1, team2, team3;

    @BeforeEach
    void setUp() {
        team1 = Team.builder().name("team1").semesterId(1L).build();
        team2 = Team.builder().name("team2").semesterId(1L).build();
        team3 = Team.builder().name("team3").semesterId(2L).build();
        teamRepository.saveAll(List.of(team1, team2, team3));
    }

    @Test
    @DisplayName("기수별 프로젝트 목록 조회")
    void find_projects_by_semester() {
        // given
        Project project1 = createProject(MAIN, team1);
        Project project2 = createProject(MAIN, team2);
        Project project3 = createProject(MAIN, team3);
        projectRepository.saveAll(List.of(project1, project2, project3));

        // when
        Page<ProjectResponse> result =
                projectRepository.findProjectsByCategoryAndSemester(MAIN, 1L, PageRequest.of(0, 30));

        // then
        assertThat(result).isNotNull();

        List<ProjectResponse> responses = result.getContent();
        assertThat(responses).hasSize(2);

        ProjectResponse firstResponse = responses.get(0);
        assertThat(firstResponse.id()).isEqualTo(2L);
        assertThat(firstResponse.name()).isEqualTo("projectName");
        assertThat(firstResponse.summary()).isEqualTo("summary");
        assertThat(firstResponse.thumbnailUrl()).isEqualTo("https://test.net/thumbnail.png");
        assertThat(firstResponse.description()).isEqualTo("description");
    }

    @Test
    @DisplayName("특정 년월에 진행한 해커톤 프로젝트 목록 조회")
    void find_hackathon_projects() {
        // given
        Project project1 = createProject(MAIN, team1);
        Project project2 = createProject(HACKATHON, team2);
        Project project3 = createProject(HACKATHON, team3);
        Project project4 = createProject(HACKATHON, team1);
        projectRepository.saveAll(List.of(project1, project2, project3, project4));

        // when
        Page<ProjectResponse> result =
                projectRepository.findProjectsByCategoryAndSemester(HACKATHON, 1L, PageRequest.of(0, 30));

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("techStack이 List<String>와 JSON 문자열 간 정상 변환됨")
    void convert_tech_stack() {
        // given
        List<String> techStack = List.of("Java", "Spring Boot", "MySQL", "JPA");

        Project project = Project.builder()
                .name("name")
                .category(Project.Category.MAIN)
                .summary("summary")
                .techStack(techStack)
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .team(team1)
                .build();

        // when
        Project saved = projectRepository.save(project);
        Project found = projectRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getTechStack()).containsExactly("Java", "Spring Boot", "MySQL", "JPA");
    }

    @Test
    @DisplayName("semesterId가 null이면 전체 조회")
    void find_all_projects_by_semester_id_null() {
        // given
        Project project1 = createProject(MAIN, team1);
        Project project2 = createProject(MAIN, team2);
        Project project3 = createProject(MAIN, team3);
        Project project4 = createProject(MAIN, team3);
        projectRepository.saveAll(List.of(project1, project2, project3, project4));

        // when
        Page<ProjectResponse> result =
                projectRepository.findProjectsByCategoryAndSemester(MAIN, null, PageRequest.of(0, 30));

        // then
        assertThat(result).isNotNull();

        List<ProjectResponse> responses = result.getContent();
        assertThat(responses).hasSize(4);
    }

    private Project createProject(Project.Category category, Team team) {
        return Project.builder()
                .name("projectName")
                .thumbnailUrl("https://test.net/thumbnail.png")
                .summary("summary")
                .description("description")
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 6, 30))
                .category(category)
                .team(team)
                .build();
    }

}
