package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.entity.TeamMember;
import org.ject.support.domain.recruit.domain.ApplicationForm;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.ApplicationFormRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MemberQueryRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ApplicationFormRepository applicationFormRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    private Team teamA;
    private Member pd1, fe1, be1, be2;
    private TeamMember teamApd1, teamAfe1, teamAbe1, teamAbe2;

    @BeforeEach
    void setUp() {
        teamA = createTeam("teamA");
        teamRepository.save(teamA);

        pd1 = createMember("김젝트", "01011112223", "pd1Email", PD);
        fe1 = createMember("박젝트", "01011112224", "fe1Email", FE);
        be1 = createMember("최젝트", "01011112225", "be1Email", BE);
        be2 = createMember("왕젝트", "01011112226", "be2Email", BE);
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
        TeamMemberNames teamMemberNames = memberRepository.findMemberNamesByTeamId(teamA.getId());

        // then
        assertThat(teamMemberNames.productManagers()).hasSize(0);
        assertThat(teamMemberNames.productDesigners()).hasSize(1);
        assertThat(teamMemberNames.frontendDevelopers()).hasSize(1);
        assertThat(teamMemberNames.backendDevelopers()).hasSize(2);
    }

    @Test
    @DisplayName("전달 받은 ID 중 지원서를 제출하지 않은 사용자의 이메일 목록 조회")
    void find_emails_by_ids_and_not_apply() {
        // given
        Member be5 = createMember("이젝트", "01011112231", "be1@test.kr", BE); // 5
        Member be6 = createMember("박젝트", "01011112232", "be2@test.kr", BE); // 6
        Member pm7 = createMember("서젝트", "01011112233", "pm1@test.kr", PM); // 7
        Member fe8 = createMember("양젝트", "01011112234", "fe1@test.kr", FE); // 8
        Member be9 = createMember("조젝트", "01011112235", "be3@test.kr", BE); // 9
        Member pd10 = createMember("표젝트", "01011112236", "pd1@test.kr", PD); // 10
        memberRepository.saveAll(List.of(be5, be6, pm7, fe8, be9, pd10));

        Recruit pmRecruit = createRecruit(PM);
        Recruit pdRecruit = createRecruit(PD);
        Recruit feRecruit = createRecruit(FE);
        Recruit beRecruit = createRecruit(BE);
        recruitRepository.saveAll(List.of(pmRecruit, pdRecruit, feRecruit, beRecruit));

        applicationFormRepository.saveAll(List.of(
                createApplicationForm(be5, beRecruit),
                createApplicationForm(pm7, pmRecruit),
                createApplicationForm(fe8, feRecruit)));

        List<Long> applicantIds = Stream.iterate(1L, id -> id + 1L).limit(10).toList();

        // when
        List<String> result = memberRepository.findEmailsByIdsAndNotApply(applicantIds);

        // then
        assertThat(result).hasSize(7);
    }

    private Team createTeam(String name) {
        return Team.builder()
                .name(name)
                .build();
    }

    private Member createMember(String name, String phoneNumber, String email, JobFamily jobFamily) {
        return Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .semesterId(1L)
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

    private Recruit createRecruit(JobFamily jobFamily) {
        return Recruit.builder()
                .semesterId(1L)
                .jobFamily(jobFamily)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
    }

    private ApplicationForm createApplicationForm(Member member, Recruit recruit) {
        return ApplicationForm.builder()
                .content("content")
                .member(member)
                .recruit(recruit)
                .build();
    }
}
