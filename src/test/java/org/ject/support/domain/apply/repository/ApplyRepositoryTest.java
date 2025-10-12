package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.Apply.Status.TEMP_SAVED;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ApplyRepositoryTest {

    @Autowired
    ApplyRepository applyRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    RecruitRepository recruitRepository;

    Semester semester;
    Recruit pmRecruit;
    Recruit pdRecruit;
    Recruit feRecruit;
    Recruit beRecruit;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        semester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());
        pmRecruit = getRecruit(PM);
        pdRecruit = getRecruit(PD);
        feRecruit = getRecruit(FE);
        beRecruit = getRecruit(BE);
        recruitRepository.saveAll(List.of(pmRecruit, pdRecruit, feRecruit, beRecruit));
    }

    @Test
    void 모집과_지원상태를_바탕으로_임시저장_상태의_지원정보_조회_성공() {
        // given
        Member feApplicant = createMember("emailFE@test.com", Role.APPLY);
        Member be1Applicant = createMember("emailBE1@test.com", Role.APPLY);
        Member be2Applicant = createMember("emailBE2@test.com", Role.APPLY);
        Member be3Applicant = createMember("emailBE3@test.com", Role.APPLY);
        memberRepository.saveAll(List.of(feApplicant, be1Applicant, be2Applicant, be3Applicant));

        Apply feTempApply = getApply(feApplicant, feRecruit, TEMP_SAVED);
        Apply be1SubmitApply = getApply(be1Applicant, beRecruit, SUBMITTED);
        Apply be2TempApply = getApply(be2Applicant, beRecruit, TEMP_SAVED);
        Apply be3TempApply = getApply(be3Applicant, beRecruit, TEMP_SAVED);
        applyRepository.saveAll(List.of(feTempApply, be1SubmitApply, be2TempApply, be3TempApply));

        // when
        List<Apply> result = applyRepository.findByRecruitAndStatus(beRecruit, TEMP_SAVED);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(be2TempApply, be3TempApply);
    }

    private Recruit getRecruit(JobFamily jobFamily) {
        return Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .build();
    }

    private Apply getApply(Member member, Recruit recruit, Apply.Status status) {
        return Apply.builder()
                .recruit(recruit)
                .member(member)
                .status(status)
                .build();
    }

    private Member createMember(String email, Role role) {
        return Member.builder()
                .email(email)
                .semesterId(1L)
                .role(role)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}