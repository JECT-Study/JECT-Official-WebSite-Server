package org.ject.support.domain.member.repository;

import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.entity.TeamMember;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MemberQueryRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Team teamA;
    private Member pd1, fe1, be1, be2;
    private TeamMember teamApd1, teamAfe1, teamAbe1, teamAbe2;

    @BeforeEach
    void setUp() {
        teamA = createTeam("teamA");
        teamRepository.save(teamA);

        pd1 = createMember("김젝트", "01011112223", "pd1Email", "1기", PD);
        fe1 = createMember("박젝트", "01011112224", "fe1Email", "1기", FE);
        be1 = createMember("최젝트", "01011112225", "be1Email", "1기", BE);
        be2 = createMember("왕젝트", "01011112226", "be2Email", "1기", BE);
        memberRepository.saveAll(List.of(pd1, fe1, be1, be2));

        teamApd1 = createTeamMember(teamA, pd1);
        teamAfe1 = createTeamMember(teamA, fe1);
        teamAbe1 = createTeamMember(teamA, be1);
        teamAbe2 = createTeamMember(teamA, be2);
        teamMemberRepository.saveAll(List.of(teamApd1, teamAfe1, teamAbe1, teamAbe2));
    }

    @Test
    @DisplayName("직군별 팀원 이름 조회")
    void find_member_names_by_team_id() {
        // when
        TeamMemberNames teamMemberNames = memberRepository.findMemberNamesByTeamId(1L);

        // then
        assertThat(teamMemberNames.projectManagers()).hasSize(0);
        assertThat(teamMemberNames.productDesigners()).hasSize(1);
        assertThat(teamMemberNames.frontendDevelopers()).hasSize(1);
        assertThat(teamMemberNames.backendDevelopers()).hasSize(2);
    }

    private Team createTeam(String name) {
        return Team.builder()
                .name(name)
                .build();
    }

    private Member createMember(String name, String phoneNumber, String email, String semester, JobFamily jobFamily) {
        return Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .semester(semester)
                .jobFamily(jobFamily)
                .role(Role.USER)
                .pin("123456") // PIN 필드 추가
                .build();
    }

    private TeamMember createTeamMember(Team team, Member member) {
        return TeamMember.builder()
                .team(team)
                .member(member)
                .build();
    }
}