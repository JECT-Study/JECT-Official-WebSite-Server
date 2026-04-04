package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.apply.domain.ApplyStatus.JOINED;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.apply.domain.ApplyStatus.TEMP_SAVED;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.entity.TeamMember;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

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
    private ApplyRepository applyRepository;

    @Autowired
    private ApplicationFormRepository applicationFormRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

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

        // TeamMember 생성 시 해당 팀에서의 jobFamily 설정
        teamApd1 = createTeamMember(teamA, pd1, PD);
        teamAfe1 = createTeamMember(teamA, fe1, FE);
        teamAbe1 = createTeamMember(teamA, be1, BE);
        teamAbe2 = createTeamMember(teamA, be2, BE);
        teamMemberRepository.saveAll(List.of(teamApd1, teamAfe1, teamAbe1, teamAbe2));
    }

    @Test
    void 직군별_팀원_이름_조회() {
        // when
        TeamMemberNames teamMemberNames = memberRepository.findMemberNamesByTeamId(teamA.getId());

        // then
        assertThat(teamMemberNames.productManagers()).isEmpty();
        assertThat(teamMemberNames.productDesigners()).hasSize(1);
        assertThat(teamMemberNames.frontendDevelopers()).hasSize(1);
        assertThat(teamMemberNames.backendDevelopers()).hasSize(2);
    }

    @Test
    void TeamMember_jobFamily_기반_직군별_팀원_이름_조회() {
        // given
        // 1기에는 BE로, 2기에는 PM으로 활동
        // 1기 팀 (teamA는 setUp에서 생성됨, semesterId=1)
        // 2기 팀 생성
        Team otherTeam = teamRepository.save(Team.builder().name("otherTeam").semesterId(2L).build());

        // Member.jobFamily는 점진적 적용으로 유지되는 값
        Member member = memberRepository.save(createMember("김젝트", "01099998888", "ject@test.com", BE));

        // 1기 팀A에서는 BE로 참여
        teamMemberRepository.save(createTeamMember(teamA, member, BE));

        // 2기 팀에서는 PM으로 참여
        teamMemberRepository.save(createTeamMember(otherTeam, member, PM));

        // when
        // 2기 팀 조회
        TeamMemberNames team2ndMemberNames = memberRepository.findMemberNamesByTeamId(otherTeam.getId());

        // then
        // 2기 팀에서는 PM으로 조회
        assertThat(team2ndMemberNames.productManagers()).hasSize(1);
        assertThat(team2ndMemberNames.productManagers()).contains("김젝트");
        assertThat(team2ndMemberNames.backendDevelopers()).isEmpty();

        // when
        // 1기 팀 조회
        TeamMemberNames teamAMemberNames = memberRepository.findMemberNamesByTeamId(teamA.getId());

        // then
        // 1기 팀에서는 BE로 조회되어야 함
        assertThat(teamAMemberNames.backendDevelopers()).contains("김젝트");
    }

    @Test
    void TeamMember_jobFamily가_null이면_Member_jobFamily로_fallback() {
        // given - TeamMember.jobFamily가 null인 경우 (기존 데이터 호환성)
        Team teamC = teamRepository.save(createTeam("teamC"));

        Member fallbackMember = memberRepository.save(createMember("폴백", "01088887777", "fallback@test.com", FE));

        // TeamMember에 jobFamily를 설정하지 않음 (null)
        TeamMember teamCMember = teamMemberRepository.save(createTeamMember(teamC, fallbackMember));

        // when
        TeamMemberNames teamMemberNames = memberRepository.findMemberNamesByTeamId(teamC.getId());

        // then - Member.jobFamily 기준으로 FE에 속해야 함
        assertThat(teamMemberNames.frontendDevelopers()).hasSize(1);
        assertThat(teamMemberNames.frontendDevelopers()).contains("폴백");
    }

    @Test
    void 전달_받은_ID_중_지원서를_제출하지_않은_사용자의_이메일_목록_조회() {
        // given
        Member be5 = createMember("이젝트", "01011112231", "be5@test.kr", BE); // 5
        Member be6 = createMember("박젝트", "01011112232", "be6@test.kr", BE); // 6
        Member pm7 = createMember("서젝트", "01011112233", "pm7@test.kr", PM); // 7
        Member fe8 = createMember("양젝트", "01011112234", "fe8@test.kr", FE); // 8
        Member be9 = createMember("조젝트", "01011112235", "be9@test.kr", BE); // 9
        Member pd10 = createMember("표젝트", "01011112236", "pd10@test.kr", PD); // 10
        List<Member> testMembers = memberRepository.saveAll(List.of(be5, be6, pm7, fe8, be9, pd10));

        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());
        Recruit pmRecruit = createRecruit(savedSemester, PM);
        Recruit pdRecruit = createRecruit(savedSemester, PD);
        Recruit feRecruit = createRecruit(savedSemester, FE);
        Recruit beRecruit = createRecruit(savedSemester, BE);
        recruitRepository.saveAll(List.of(pmRecruit, pdRecruit, feRecruit, beRecruit));

        // 일부 회원만 지원서 제출
        Apply be5Apply = applyRepository.save(createApply(beRecruit, be5, SUBMITTED));
        applyRepository.save(createApply(beRecruit, be6, JOINED));
        Apply pm7Apply = applyRepository.save(createApply(pmRecruit, pm7, SUBMITTED));
        Apply fe8Apply = applyRepository.save(createApply(feRecruit, fe8, SUBMITTED));
        applyRepository.save(createApply(beRecruit, be9, JOINED));
        applyRepository.save(createApply(pdRecruit, pd10, TEMP_SAVED));

        applicationFormRepository.saveAll(List.of(
                createApplicationForm(be5Apply),
                createApplicationForm(pm7Apply),
                createApplicationForm(fe8Apply)));

        List<Long> applicantIds = testMembers.stream().map(Member::getId).toList();

        // when
        List<String> result = memberRepository.findEmailsByIdsAndNotSubmitted(applicantIds);

        // then
        assertThat(result).hasSize(3)
                .containsExactlyInAnyOrder(be6.getEmail(), be9.getEmail(), pd10.getEmail());
    }


    private Semester createSemester(String name) {
        return Semester.builder()
                .name(name)
                .isRecruiting(true)
                .build();
    }

    private Member createMemberWithSemester(String name, String phoneNumber, String email, JobFamily jobFamily, Role role, Long semesterId) {
        return Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .semesterId(semesterId)
                .jobFamily(jobFamily)
                .role(role)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private Team createTeam(String name) {
        return Team.builder()
                .name(name)
                .semesterId(1L)
                .build();
    }

    private Member createMember(String name, String phoneNumber, String email, JobFamily jobFamily) {
        return Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .semesterId(1L)
                .jobFamily(jobFamily)
                .role(Role.SEMESTER)
                .pin("123456") // PIN 필드 추가
                .status(MemberStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    private TeamMember createTeamMember(Team team, Member member) {
        return TeamMember.builder()
                .team(team)
                .member(member)
                .build();
    }

    private TeamMember createTeamMember(Team team, Member member, JobFamily jobFamily) {
        return TeamMember.builder()
                .team(team)
                .member(member)
                .jobFamily(jobFamily)
                .build();
    }

    private Recruit createRecruit(Semester semester, JobFamily jobFamily) {
        return Recruit.builder()
                .semester(semester)
                .jobFamily(jobFamily)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
    }

    private Apply createApply(Recruit recruit, Member member, ApplyStatus status) {
        return applyRepository.save(Apply.builder()
                .member(member)
                .recruit(recruit)
                .status(status)
                .build());
    }

    private ApplicationForm createApplicationForm(Apply apply) {
        return ApplicationForm.builder()
                .content("content")
                .apply(apply)
                .build();
    }
}
