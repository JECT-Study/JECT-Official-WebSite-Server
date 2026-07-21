package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.entity.MemberSemester;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
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

    private MemberSemesterSearchCondition searchCondition(
        Long cursor,
        Integer size,
        Long semesterId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        CareerDetails careerDetails,
        Long teamId,
        ActivityStatus status
    ) {
        return new MemberSemesterSearchCondition(
            cursor,
            size,
            semesterId,
            jobFamily,
            recruitTypeDetail,
            careerDetails,
            teamId,
            status
        );
    }

    private MemberActivity saveSemesterActivity(
        String email,
        Long semesterId,
        Long teamId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        CareerDetails careerDetails,
        ExperiencePeriod experiencePeriod,
        ActivityStatus activityStatus
    ) {
        Member member = memberRepository.save(member().email(email).build());
        return memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(member.getId())
            .semesterId(semesterId)
            .teamId(teamId)
            .jobFamily(jobFamily)
            .recruitTypeDetail(recruitTypeDetail)
            .careerDetails(careerDetails)
            .experiencePeriod(experiencePeriod)
            .activityStatus(activityStatus)
            .build());
    }

    private MemberActivity saveSemesterActivity(
        String name,
        String email,
        Long semesterId,
        Long teamId,
        JobFamily jobFamily
    ) {
        Member member = memberRepository.save(member().name(name).email(email).build());
        return memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(member.getId())
            .semesterId(semesterId)
            .teamId(teamId)
            .jobFamily(jobFamily)
            .build());
    }

    private MemberActivity saveSemesterActivity(
        String email,
        Long semesterId,
        Long teamId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        CareerDetails careerDetails,
        ExperiencePeriod experiencePeriod
    ) {
        return saveSemesterActivity(
            email,
            semesterId,
            teamId,
            jobFamily,
            recruitTypeDetail,
            careerDetails,
            experiencePeriod,
            ActivityStatus.ACTIVE
        );
    }
    /**
     * 일반 구성원 추가 테스트
     */
    @Test
    @DisplayName("일반 구성원 활동을 저장하면 하위 테이블에 기수 정보를 함께 저장한다")
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
    @DisplayName("같은 구성원의 동일 기수 활동이 존재하면 true를 반환한다")
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
    @DisplayName("다른 기수의 활동만 존재하면 false를 반환한다")
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
    @DisplayName("다른 구성원의 활동만 존재하면 false를 반환한다")
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
    @DisplayName("다른 구성원 유형으로 조회하면 false를 반환한다")
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

    /**
     * 일반 구성원 목록 조회 테스트
     */
    // 목록 조회 (필터 X)
    @Test
    void 일반_구성원_목록을_최신순으로_조회한다() {
        // given
        MemberActivity first = saveSemesterActivity(
            "member1@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberActivity second = saveSemesterActivity(
            "member2@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.FE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.STUDENT,
            ExperiencePeriod.NONE
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            null,
            null,
            null,
            null,
            null,
            null
        );

        // when
        List<SearchMemberSemesterProjection> results =
            memberActivityRepository.searchMemberSemesters(condition, 30);

        // then
        assertThat(results)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(second.getId(), first.getId());
    }

    // 목록 조회(커서)
    @Test
    @DisplayName("커서보다 작은 일반 구성원 목록을 조회한다")
    void 커서보다_작은_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity first = saveSemesterActivity(
            "member1@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberActivity second = saveSemesterActivity(
            "member2@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.FE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.STUDENT,
            ExperiencePeriod.NONE
        );
        MemberSemesterSearchCondition condition = searchCondition(
            second.getId(),
            30,
            null,
            null,
            null,
            null,
            null,
            null
        );

        // when
        List<SearchMemberSemesterProjection> results =
            memberActivityRepository.searchMemberSemesters(condition, 30);

        // then
        assertThat(results)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(first.getId());
    }

    // 목록 조회(전체 필터 조합)
    @Test
    @DisplayName("전체 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 전체_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-job@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.FE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-semester@test.com",
            6L,
            2L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            SEMESTER_ID,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            1L,
            null
        );

        // when
        List<SearchMemberSemesterProjection> results =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);

        // then
        assertThat(results)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
    }

    // 목록 조회(기수 필터)
    @Test
    @DisplayName("기수 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 기수_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-semester@test.com",
            10L,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            SEMESTER_ID,
            null,
            null,
            null,
            null,
            null
        );
        // when
        List<SearchMemberSemesterProjection> result =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        // then
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
    }

    // 목록 조회(직군 필터)
    @Test
    @DisplayName("직군 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 직군_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-job-family@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.FE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            null,
            JobFamily.BE,
            null,
            null,
            null,
            null
        );
        // when
        List<SearchMemberSemesterProjection> result =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        // then
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
    }
    // 목록 조회(모집 단위 필터)
    @Test
    @DisplayName("모집 단위 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 모집_단위_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-recruit-type@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REFILL,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            null,
            null,
            RecruitTypeDetail.REGULAR,
            null,
            null,
            null
        );
        // when
        List<SearchMemberSemesterProjection> result =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        // then
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
    }
    // 목록 조회(신분 필터)
    @Test
    @DisplayName("신분 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 신분_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-career@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.STUDENT,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            null,
            null,
            null,
            CareerDetails.EMPLOYEE,
            null,
            null
        );
        // when
        List<SearchMemberSemesterProjection> result =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        // then
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
    }
    // 목록 조회(팀 필터)
    @Test
    @DisplayName("팀 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 팀_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "other-team@test.com",
            SEMESTER_ID,
            4L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            null,
            null,
            null,
            null,
            1L,
            null
        );
        // when
        List<SearchMemberSemesterProjection> result =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        // then
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
    }

    // 목록 조회(활동 상태 필터)
    @Test
    @DisplayName("활동 상태 필터 조건에 맞는 일반 구성원 목록을 조회한다")
    void 활동_상태_필터_조건에_맞는_일반_구성원_목록을_조회한다() {
        // given
        MemberActivity matched = saveSemesterActivity(
            "matched@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO,
            ActivityStatus.COMPLETED
        );
        saveSemesterActivity(
            "other-status@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO,
            ActivityStatus.ACTIVE
        );
        MemberSemesterSearchCondition condition = searchCondition(
            null,
            30,
            null,
            null,
            null,
            null,
            null,
            ActivityStatus.COMPLETED
        );

        // when
        List<SearchMemberSemesterProjection> result =
            memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);

        // then
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::memberActivityId)
            .containsExactly(matched.getId());
        assertThat(result)
            .extracting(SearchMemberSemesterProjection::status)
            .containsExactly(ActivityStatus.COMPLETED);
    }

    @Test
    void 카운트_조회는_커서_조건을_적용하지_않는다() {
        // given
        saveSemesterActivity(
            "member1@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberActivity cursor = saveSemesterActivity(
            "member2@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        saveSemesterActivity(
            "member3@test.com",
            SEMESTER_ID,
            1L,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO
        );
        MemberSemesterSearchCondition condition = searchCondition(
            cursor.getId(),
            30,
            SEMESTER_ID,
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            1L,
            null
        );

        // when
        List<SearchMemberSemesterProjection> results =
            memberActivityRepository.searchMemberSemesters(condition, 30);
        long totalCount = memberActivityRepository.countMemberSemesters(condition);

        // then
        assertThat(results).hasSize(1);
        assertThat(totalCount).isEqualTo(3L);
    }

    @Test
    @DisplayName("팀 ID로 일반 구성원 이름을 직군별로 조회한다")
    void 팀_ID로_일반_구성원_이름을_직군별로_조회한다() {
        // given
        Long teamId = 10L;
        saveSemesterActivity("기획자", "pm@test.com", SEMESTER_ID, teamId, JobFamily.PM);
        saveSemesterActivity("디자이너", "pd@test.com", SEMESTER_ID, teamId, JobFamily.PD);
        saveSemesterActivity("프론트1", "fe1@test.com", SEMESTER_ID, teamId, JobFamily.FE);
        saveSemesterActivity("프론트2", "fe2@test.com", SEMESTER_ID, teamId, JobFamily.FE);
        saveSemesterActivity("백엔드", "be@test.com", SEMESTER_ID, teamId, JobFamily.BE);
        saveSemesterActivity("앱", "app@test.com", SEMESTER_ID, teamId, JobFamily.APP);
        saveSemesterActivity("다른팀", "other@test.com", SEMESTER_ID, 20L, JobFamily.BE);

        // when
        TeamMemberNames result = memberActivityRepository.findMemberNamesByTeamId(teamId);

        // then
        assertThat(result.productManagers()).containsExactly("기획자");
        assertThat(result.productDesigners()).containsExactly("디자이너");
        assertThat(result.frontendDevelopers()).containsExactly("프론트1", "프론트2");
        assertThat(result.backendDevelopers()).containsExactly("백엔드");
    }

    @Test
    @DisplayName("구성원이 없는 팀은 직군별 빈 목록을 반환한다")
    void 구성원이_없는_팀은_직군별_빈_목록을_반환한다() {
        // when
        TeamMemberNames result = memberActivityRepository.findMemberNamesByTeamId(999L);

        // then
        assertThat(result.productManagers()).isEmpty();
        assertThat(result.productDesigners()).isEmpty();
        assertThat(result.frontendDevelopers()).isEmpty();
        assertThat(result.backendDevelopers()).isEmpty();
    }

    @Test
    @DisplayName("활동 상태가 탈퇴인 일반 구성원은 팀원 이름 조회에서 제외한다")
    void 활동_상태가_탈퇴인_일반_구성원은_팀원_이름_조회에서_제외한다() {
        // given
        Long teamId = 10L;
        saveSemesterActivity("정상 구성원", "active-name@test.com", SEMESTER_ID, teamId, JobFamily.BE);

        Member withdrawnMember = memberRepository.save(
            member().name("탈퇴 구성원").email("withdrawn-name@test.com").build()
        );
        memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(withdrawnMember.getId())
            .semesterId(SEMESTER_ID)
            .teamId(teamId)
            .jobFamily(JobFamily.BE)
            .activityStatus(ActivityStatus.WITHDRAWN)
            .build());

        // when
        TeamMemberNames result = memberActivityRepository.findMemberNamesByTeamId(teamId);

        // then
        assertThat(result.backendDevelopers()).containsExactly("정상 구성원");
    }

    @Test
    @DisplayName("삭제된 구성원과 활동은 팀원 이름 조회에서 제외한다")
    void 삭제된_구성원과_활동은_팀원_이름_조회에서_제외한다() {
        // given
        Long teamId = 10L;
        saveSemesterActivity("정상 구성원", "active@test.com", SEMESTER_ID, teamId, JobFamily.BE);

        Member deletedMember = memberRepository.save(
            member().name("삭제 구성원").email("deleted-member@test.com").build()
        );
        memberActivityRepository.saveAndFlush(semesterActivity()
            .memberId(deletedMember.getId())
            .semesterId(SEMESTER_ID)
            .teamId(teamId)
            .jobFamily(JobFamily.BE)
            .build());
        memberRepository.delete(deletedMember);

        MemberActivity deletedActivity = saveSemesterActivity(
            "삭제 활동",
            "deleted-activity@test.com",
            SEMESTER_ID,
            teamId,
            JobFamily.BE
        );
        memberActivityRepository.delete(deletedActivity);
        entityManager.flush();
        entityManager.clear();

        // when
        TeamMemberNames result = memberActivityRepository.findMemberNamesByTeamId(teamId);

        // then
        assertThat(result.backendDevelopers()).containsExactly("정상 구성원");
    }
}
