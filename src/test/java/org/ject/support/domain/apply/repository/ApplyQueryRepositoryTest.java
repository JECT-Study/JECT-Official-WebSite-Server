package org.ject.support.domain.apply.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.Apply.Status.TEMP_SAVED;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.domain.apply.domain.ApplicationForm;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ApplyQueryRepositoryTest {

    @Autowired
    ApplyRepository applyRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    MemberRepository memberRepository;

    Semester semester;
    Recruit beRecruit;
    Recruit feRecruit;
    Recruit pmRecruit;

    @BeforeEach
    void setUp() {
        semester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        beRecruit = getRecruit(BE);
        feRecruit = getRecruit(FE);
        pmRecruit = getRecruit(PM);
        recruitRepository.saveAll(List.of(beRecruit, feRecruit, pmRecruit));
    }

    @Test
    void JobFamily로_제출된_지원서_목록_조회_성공() {
        // given
        Member beMember1 = createMember("be1@test.com", BE);
        Member beMember2 = createMember("be2@test.com", BE);
        Member feMember = createMember("fe@test.com", FE);
        memberRepository.saveAll(List.of(beMember1, beMember2, feMember));

        Apply beApply1 = getApply(beMember1, beRecruit, SUBMITTED);
        Apply beApply2 = getApply(beMember2, beRecruit, SUBMITTED);
        Apply feApply = getApply(feMember, feRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(beApply1, beApply2, feApply));

        Pageable pageable = PageRequest.of(0, 15);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(BE, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Apply::getMember)
                .containsExactlyInAnyOrder(beMember1, beMember2);
    }

    @Test
    void JobFamily가_null이면_전체_제출된_지원서_조회() {
        // given
        Member beMember = createMember("be@test.com", BE);
        Member feMember = createMember("fe@test.com", FE);
        Member pmMember = createMember("pm@test.com", PM);
        memberRepository.saveAll(List.of(beMember, feMember, pmMember));

        Apply beApply = getApply(beMember, beRecruit, SUBMITTED);
        Apply feApply = getApply(feMember, feRecruit, SUBMITTED);
        Apply pmApply = getApply(pmMember, pmRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(beApply, feApply, pmApply));

        Pageable pageable = PageRequest.of(0, 15);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void 임시저장_상태는_조회되지_않음() {
        // given
        Member beMember1 = createMember("be1@test.com", BE);
        Member beMember2 = createMember("be2@test.com", BE);
        memberRepository.saveAll(List.of(beMember1, beMember2));

        Apply submittedApply = getApply(beMember1, beRecruit, SUBMITTED);
        Apply tempApply = getApply(beMember2, beRecruit, TEMP_SAVED);
        applyRepository.saveAll(List.of(submittedApply, tempApply));

        Pageable pageable = PageRequest.of(0, 15);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(BE, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void 삭제된_회원의_지원서는_조회되지_않음() {
        // given
        Member activeMember = createMember("active@test.com", BE);
        Member deletedMember = createMember("deleted@test.com", BE);
        deletedMember.deleteProfile();
        memberRepository.saveAll(List.of(activeMember, deletedMember));

        Apply activeApply = getApply(activeMember, beRecruit, SUBMITTED);
        Apply deletedApply = getApply(deletedMember, beRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(activeApply, deletedApply));

        Pageable pageable = PageRequest.of(0, 15);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(BE, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getMember()).isEqualTo(activeMember);
    }

    @Test
    void 페이징_정상_작동() {
        // given
        for (int i = 1; i <= 25; i++) {
            Member member = createMember("be" + i + "@test.com", BE);
            memberRepository.save(member);
            Apply apply = getApply(member, beRecruit, SUBMITTED);
            applyRepository.save(apply);
        }

        Pageable pageable = PageRequest.of(1, 10);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(BE, pageable);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
    }

    @Test
    void 제출된_지원서가_없으면_빈_페이지_반환() {
        // given
        Pageable pageable = PageRequest.of(0, 15);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(BE, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void createdAt_기준_내림차순_정렬()  {
        // given
        Member member1 = createMember("be1@test.com", BE);
        Member member2 = createMember("be2@test.com", BE);
        Member member3 = createMember("be3@test.com", BE);
        memberRepository.saveAll(List.of(member1, member2, member3));

        Apply apply1 = getApply(member1, beRecruit, SUBMITTED);
        applyRepository.save(apply1);

        Apply apply2 = getApply(member2, beRecruit, SUBMITTED);
        applyRepository.save(apply2);

        Apply apply3 = getApply(member3, beRecruit, SUBMITTED);
        applyRepository.save(apply3);

        Pageable pageable = PageRequest.of(0, 15);

        // when
        Page<Apply> result = applyRepository.findSubmittedApplies(BE, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0)).isEqualTo(apply3);
        assertThat(result.getContent().get(1)).isEqualTo(apply2);
        assertThat(result.getContent().get(2)).isEqualTo(apply1);
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
        ApplicationForm applicationForm = ApplicationForm.builder()
                .build();

        Apply apply = Apply.builder()
                .recruit(recruit)
                .member(member)
                .status(status)
                .applicationForm(applicationForm)
                .build();

        setField(applicationForm, "apply", apply);

        return apply;
    }

    private Member createMember(String email, JobFamily jobFamily) {
        return Member.builder()
                .email(email)
                .jobFamily(jobFamily)
                .semesterId(1L)
                .role(Role.APPLY)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
