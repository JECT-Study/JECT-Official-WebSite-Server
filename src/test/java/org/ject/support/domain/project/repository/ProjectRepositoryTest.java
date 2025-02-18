package org.ject.support.domain.project.repository;

import org.ject.support.domain.member.*;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.domain.project.exception.ProjectErrorCode;
import org.ject.support.domain.project.exception.ProjectException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.*;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Team team1, team2, team3;

    @BeforeEach
    void setUp() {
        team1 = createTeam("team1");
        team2 = createTeam("team2");
        team3 = createTeam("team3");
        teamRepository.saveAll(List.of(team1, team2, team3));
    }

    @Test
    @DisplayName("기수별 프로젝트 목록 조회")
    void find_projects_by_semester() {
        // given
        Project project1 = createProject("1기", team1);
        Project project2 = createProject("1기", team2);
        Project project3 = createProject("2기", team3);
        projectRepository.saveAll(List.of(project1, project2, project3));

        // when
        Page<ProjectResponse> result = projectRepository.findProjectsBySemester("1기", PageRequest.of(0, 20));

        // then
        assertThat(result).isNotNull();

        List<ProjectResponse> responses = result.getContent();
        assertThat(responses).hasSize(2);

        ProjectResponse firstResponse = responses.get(0);
        assertThat(firstResponse.name()).isEqualTo("projectName");
        assertThat(firstResponse.summary()).isEqualTo("summary");
        assertThat(firstResponse.thumbnailUrl()).isEqualTo("https://test.net/thumbnail.png");
        assertThat(firstResponse.startDate()).isEqualTo(LocalDate.of(2025, 3, 2));
        assertThat(firstResponse.endDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    @DisplayName("프로젝트 상세 정보 조회")
    void find_project_details() {
        // given
        Member productDesigner1 = createMember("productDesigner1", "01011111111", "productDesigner1@test.com", PD, "1기");
        Member frontendDev1 = createMember("frontendDev1", "01011111112", "frontendDev1@test.com", FE, "1기");
        Member frontendDev2 = createMember("frontendDev2", "01011111113", "frontendDev2@test.com", FE, "1기");
        Member backendDev1 = createMember("backendDev1", "01011111114", "backendDev1@test.com", BE, "1기");
        Member backendDev2 = createMember("backendDev2", "01011111115", "backendDev2@test.com", BE, "2기");
        memberRepository.saveAll(List.of(productDesigner1, frontendDev1, frontendDev2, backendDev1, backendDev2));

        TeamMember teamMember1 = createTeamMember(team1, productDesigner1);
        TeamMember teamMember2 = createTeamMember(team1, frontendDev1);
        TeamMember teamMember3 = createTeamMember(team1, frontendDev2);
        TeamMember teamMember4 = createTeamMember(team1, backendDev1);
        TeamMember teamMember5 = createTeamMember(team2, backendDev2);
        teamMemberRepository.saveAll(List.of(teamMember1, teamMember2, teamMember3, teamMember4, teamMember5));

        Project project = createProject("1기", team1);
        Project saved = projectRepository.save(project);

        // when
        ProjectDetailResponse result = projectRepository.findProjectDetails(saved.getId())
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND));

        // then
        assertThat(result.name()).isEqualTo("projectName");
        assertThat(result.thumbnailUrl()).isEqualTo("https://test.net/thumbnail.png");
        assertThat(result.projectManagers()).hasSize(0);
        assertThat(result.productDesigners()).containsExactly("productDesigner1");
        assertThat(result.frontendDevelopers()).containsExactly("frontendDev1", "frontendDev2");
        assertThat(result.backendDevelopers()).containsExactly("backendDev1");
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2025, 3, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 상세 정보 조회")
    void find_project_details_not_found() {
        // when
        Optional<ProjectDetailResponse> result = projectRepository.findProjectDetails(1L);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    private Team createTeam(String teamName) {
        return Team.builder()
                .name(teamName)
                .build();
    }

    private Member createMember(String memberName, String phoneNumber, String email, JobFamily jobFamily, String semester) {
        return Member.builder()
                .name(memberName)
                .phoneNumber(phoneNumber)
                .email(email)
                .jobFamily(jobFamily)
                .semester(semester)
                .role(Role.USER)
                .build();
    }

    private TeamMember createTeamMember(Team team, Member member) {
        return TeamMember.builder()
                .team(team)
                .member(member)
                .build();
    }

    private Project createProject(String semester, Team team) {
        return Project.builder()
                .name("projectName")
                .thumbnailUrl("https://test.net/thumbnail.png")
                .semester(semester)
                .summary("summary")
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 6, 30))
                .team(team)
                .build();
    }
}