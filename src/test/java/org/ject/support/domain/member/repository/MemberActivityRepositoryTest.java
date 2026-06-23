package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;

import jakarta.persistence.EntityManager;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.entity.MemberSemester;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MemberActivityRepositoryTest {

    private static final Long SEMESTER_ID = 5L;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberActivityRepository memberActivityRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void 일반_구성원_활동을_저장하면_하위_테이블에_기수_정보를_함께_저장한다() {
        // given
        Member member = memberRepository.save(member().email("member@test.com").build());
        MemberActivity activity = semesterActivity()
            .memberId(member.getId())
            .semesterId(SEMESTER_ID)
            .build();

        // when
        MemberActivity savedActivity = memberActivityRepository.saveAndFlush(activity);
        entityManager.clear();

        MemberActivity result = memberActivityRepository.findById(savedActivity.getId())
            .orElseThrow();
        MemberSemester memberSemester = result.getMemberSemester();

        // then
        assertThat(memberSemester).isNotNull();
        assertThat(memberSemester.getId()).isEqualTo(result.getId());
        assertThat(memberSemester.getSemesterId()).isEqualTo(SEMESTER_ID);
        assertThat(memberSemester.getTeamId()).isEqualTo(3L);
        assertThat(memberSemester.getMemberActivity()).isSameAs(result);
    }

    @Test
    void 같은_구성원의_동일_기수_활동이_존재하면_true를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("member@test.com").build());
        memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(member.getId())
            .semesterId(SEMESTER_ID)
            .build());

        // when
        boolean result = memberActivityRepository.existsSemesterActivity(
            member.getId(),
            MemberType.SEMESTER,
            SEMESTER_ID
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 다른_기수의_활동만_존재하면_false를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("member@test.com").build());
        memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(member.getId())
            .semesterId(SEMESTER_ID)
            .build());

        // when
        boolean result = memberActivityRepository.existsSemesterActivity(
            member.getId(),
            MemberType.SEMESTER,
            6L
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 다른_구성원의_활동만_존재하면_false를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("member@test.com").build());
        Member otherMember = memberRepository.save(member().email("other-member@test.com").build());
        memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(member.getId())
            .semesterId(SEMESTER_ID)
            .build());

        // when
        boolean result = memberActivityRepository.existsSemesterActivity(
            otherMember.getId(),
            MemberType.SEMESTER,
            SEMESTER_ID
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 다른_구성원_유형으로_조회하면_false를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("member@test.com").build());
        memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(member.getId())
            .semesterId(SEMESTER_ID)
            .build());

        // when
        boolean result = memberActivityRepository.existsSemesterActivity(
            member.getId(),
            MemberType.MAKERS,
            SEMESTER_ID
        );

        // then
        assertThat(result).isFalse();
    }
}
