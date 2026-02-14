package org.ject.support.domain.member.repository;

import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.admin.dto.MemberResponse;
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
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.apply.domain.Apply.Status.JOINED;
import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.Apply.Status.TEMP_SAVED;
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

                pd1 = createMember("김젝트", "01011112223", "pd1Email");
                fe1 = createMember("박젝트", "01011112224", "fe1Email");
                be1 = createMember("최젝트", "01011112225", "be1Email");
                be2 = createMember("왕젝트", "01011112226", "be2Email");
                memberRepository.saveAll(List.of(pd1, fe1, be1, be2));

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
        void 전달_받은_ID_중_지원서를_제출하지_않은_사용자의_이메일_목록_조회() {
                // given
                Member be5 = createMember("이젝트", "01011112231", "be5@test.kr"); // 5
                Member be6 = createMember("박젝트", "01011112232", "be6@test.kr"); // 6
                Member pm7 = createMember("서젝트", "01011112233", "pm7@test.kr"); // 7
                Member fe8 = createMember("양젝트", "01011112234", "fe8@test.kr"); // 8
                Member be9 = createMember("조젝트", "01011112235", "be9@test.kr"); // 9
                Member pd10 = createMember("표젝트", "01011112236", "pd10@test.kr"); // 10
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

        @Test
        void 회원_목록_조회_구분_필터만_적용() {
                // given
                var semester1 = createSemester("1기");
                var semester2 = createSemester("2기");
                semesterRepository.saveAll(List.of(semester1, semester2));

                var admin1 = createMemberWithSemesterAndJobFamily("가젝트", "01011111111", "admin1@test.com", BE,
                                Role.ADMIN,
                                semester1);
                var admin2 = createMemberWithSemesterAndJobFamily("나젝트", "01011111112", "admin2@test.com", FE,
                                Role.ADMIN,
                                semester1);
                var semester1Member = createMemberWithSemesterAndJobFamily("다젝트", "01011111113", "semester1@test.com",
                                BE,
                                Role.SEMESTER, semester1);
                var apply1 = createMemberWithSemesterAndJobFamily("라젝트", "01011111114", "apply1@test.com", PD,
                                Role.APPLY,
                                semester2);

                var pageable = PageRequest.of(0, 15);

                // when
                var result = memberRepository.findMembers(Role.ADMIN, null, null, pageable);

                // then
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);
                assertThat(result.getContent())
                                .extracting(MemberResponse::name)
                                .containsExactly(admin2.getName(), admin1.getName()); // createdAt desc 순서
        }

        @Test
        void 회원_목록_조회_구분과_직군_필터_적용() {
                // given
                var semester1 = createSemester("1기");
                semesterRepository.save(semester1);

                var admin1 = createMemberWithSemesterAndJobFamily("가젝트", "01011111111", "admin1@test.com", BE,
                                Role.ADMIN,
                                semester1);
                var admin2 = createMemberWithSemesterAndJobFamily("나젝트", "01011111112", "admin2@test.com", FE,
                                Role.ADMIN,
                                semester1);
                var admin3 = createMemberWithSemesterAndJobFamily("다젝트", "01011111113", "admin3@test.com", BE,
                                Role.ADMIN,
                                semester1);

                var pageable = PageRequest.of(0, 15);

                // when
                var result = memberRepository.findMembers(Role.ADMIN, JobFamily.BE, null, pageable);

                // then
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);
                assertThat(result.getContent())
                                .extracting(MemberResponse::jobFamily)
                                .containsOnly(JobFamily.BE);
                assertThat(result.getContent())
                                .extracting(MemberResponse::name)
                                .containsExactly(admin3.getName(), admin1.getName()); // createdAt desc 순서
        }

        @Test
        void 회원_목록_조회_구분과_기수_필터_적용() {
                // given
                var semester1 = createSemester("1기");
                var semester2 = createSemester("2기");
                semesterRepository.saveAll(List.of(semester1, semester2));

                var semester1Member1 = createMemberWithSemesterAndJobFamily("가젝트", "01011111111", "semester1@test.com",
                                BE,
                                Role.SEMESTER, semester1);
                var semester1Member2 = createMemberWithSemesterAndJobFamily("나젝트", "01011111112", "semester2@test.com",
                                FE,
                                Role.SEMESTER, semester1);
                var semester2Member1 = createMemberWithSemesterAndJobFamily("다젝트", "01011111113", "semester3@test.com",
                                BE,
                                Role.SEMESTER, semester2);

                var pageable = PageRequest.of(0, 15);

                // when
                var result = memberRepository.findMembers(Role.SEMESTER, null, semester2.getId(), pageable);

                // then
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent())
                                .extracting(MemberResponse::semesterName)
                                .containsOnly("2");
                assertThat(result.getContent())
                                .extracting(MemberResponse::name)
                                .containsExactlyInAnyOrder(semester2Member1.getName());
        }

        @Test
        void 회원_목록_조회_모든_필터_적용() {
                // given
                var semester1 = createSemester("1기");
                var semester2 = createSemester("2기");
                semesterRepository.saveAll(List.of(semester1, semester2));

                var semester1BE1 = createMemberWithSemesterAndJobFamily("가젝트", "01011111111", "semester1be1@test.com",
                                BE,
                                Role.ADMIN, semester1);
                var semester1BE2 = createMemberWithSemesterAndJobFamily("나젝트", "01011111112", "semester1be2@test.com",
                                BE,
                                Role.ADMIN, semester1);
                var semester1FE1 = createMemberWithSemesterAndJobFamily("다젝트", "01011111113", "semester1fe1@test.com",
                                FE,
                                Role.ADMIN, semester1);
                var semester2BE1 = createMemberWithSemesterAndJobFamily("라젝트", "01011111114", "semester2be1@test.com",
                                BE,
                                Role.ADMIN, semester2);

                var pageable = PageRequest.of(0, 10);

                // when
                var result = memberRepository.findMembers(Role.ADMIN, JobFamily.BE, semester2.getId(), pageable);

                // then
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent())
                                .extracting(MemberResponse::jobFamily)
                                .containsOnly(JobFamily.BE);
                assertThat(result.getContent())
                                .extracting(MemberResponse::semesterName)
                                .containsOnly("2");
                assertThat(result.getContent())
                                .extracting(MemberResponse::name)
                                .containsExactly(semester2BE1.getName()); // createdAt desc 순서
        }

        @Test
        void 회원_목록_조회_삭제된_회원_제외() {
                // given
                var semester1 = createSemester("1기");
                semesterRepository.save(semester1);

                var deletedMember = createMemberWithSemesterAndJobFamily("삭제회원", "01011111111", "deleted@test.com", BE,
                                Role.SEMESTER, semester1);

                // 회원 삭제 (소프트 삭제)
                memberRepository.delete(deletedMember);

                var pageable = PageRequest.of(0, 10);

                // when
                var result = memberRepository.findMembers(Role.SEMESTER, null, null, pageable);

                // then
                assertThat(result.getContent())
                                .isNotEmpty()
                                .extracting(MemberResponse::name)
                                .doesNotContain(deletedMember.getName());
        }

        @Test
        void 회원_목록_조회_결과_없음() {
                // given
                var pageable = PageRequest.of(0, 10);

                // when - 존재하지 않는 Role로 조회
                var result = memberRepository.findMembers(Role.ADMIN, null, null, pageable);

                // then
                assertThat(result.getContent()).isEmpty();
                assertThat(result.getTotalElements()).isZero();
                assertThat(result.getTotalPages()).isZero();
        }

        private Semester createSemester(String name) {
                return Semester.builder()
                                .name(name)
                                .isRecruiting(true)
                                .build();
        }

        private Member createMemberWithSemesterAndJobFamily(String name, String phoneNumber, String email,
                        JobFamily jobFamily, Role role, Semester semester) {
                Member m = memberRepository.save(Member.builder()
                                .name(name)
                                .phoneNumber(phoneNumber)
                                .email(email)
                                .role(role)
                                .pin("123456")
                                .status(MemberStatus.ACTIVE)
                                .build());
                Team t = teamRepository.save(Team.builder()
                                .name(name + "_team")
                                .semesterId(semester.getId())
                                .build());
                teamMemberRepository.save(TeamMember.builder()
                                .team(t)
                                .member(m)
                                .jobFamily(jobFamily)
                                .build());
                return m;
        }

        private Team createTeam(String name) {
                return Team.builder()
                                .name(name)
                                .semesterId(1L)
                                .build();
        }

        private Member createMember(String name, String phoneNumber, String email) {
                return Member.builder()
                                .name(name)
                                .phoneNumber(phoneNumber)
                                .email(email)
                                .role(Role.SEMESTER)
                                .pin("123456") // PIN 필드 추가
                                .status(MemberStatus.ACTIVE)
                                .isDeleted(false)
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

        private Apply createApply(Recruit recruit, Member member, Apply.Status status) {
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
