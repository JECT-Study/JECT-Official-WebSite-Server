package org.ject.support.domain.project.repository;

import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.repository.TeamRepository;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void 기수별_프로젝트_목록_조회() {
        // given
        Project project1 = createProject(Project.Category.SEMESTER_1, team1);
        Project project2 = createProject(Project.Category.SEMESTER_1, team2);
        Project project3 = createProject(Project.Category.SEMESTER_1, team3);
        projectRepository.saveAll(List.of(project1, project2, project3));

        // when
        Page<ProjectResponse> result =
                projectRepository.findProjectsByCategoryAndSemester(Project.Category.SEMESTER_1, 1L, PageRequest.of(0, 30));

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
        assertThat(firstResponse.serviceType()).isEqualTo("WEB");
    }

    @Test
    void 프로젝트_카테고리중_2기가진행한_프로젝트_조회() {
        // given
        Project project1 = createProject(Project.Category.SEMESTER_1, team1);
        Project project2 = createProject(Project.Category.SEMESTER_1, team2);
        Project project3 = createProject(Project.Category.SEMESTER_2, team3);
        Project project4 = createProject(Project.Category.SEMESTER_2, team1);
        projectRepository.saveAll(List.of(project1, project2, project3, project4));

        // when
        Page<ProjectResponse> result =
                projectRepository.findProjectsByCategoryAndSemester(Project.Category.SEMESTER_1, 1L, PageRequest.of(0, 30));

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void techStack이_ListString와_JSON_문자열_간_정상_변환됨() {
        // given
        List<String> techStack = List.of("Java", "Spring Boot", "MySQL", "JPA");

        Project project = Project.builder()
                .name("name")
                .category(Project.Category.SEMESTER_1)
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
    void semesterId가_NULL이면_전체_조회() {
        // given
        Project project1 = createProject(Project.Category.SEMESTER_1, team1);
        Project project2 = createProject(Project.Category.SEMESTER_1, team2);
        Project project3 = createProject(Project.Category.SEMESTER_1, team3);
        Project project4 = createProject(Project.Category.SEMESTER_1, team3);
        projectRepository.saveAll(List.of(project1, project2, project3, project4));

        // when
        Page<ProjectResponse> result =
                projectRepository.findProjectsByCategoryAndSemester(Project.Category.SEMESTER_1, null, PageRequest.of(0, 30));

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
                .serviceType("WEB")
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 6, 30))
                .category(category)
                .team(team)
                .build();
    }
}
