package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MakersActivityFixture.*;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
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

    private MemberActivity createMakersActivity(Long memberId, ActivityStatus activityStatus) {
        MemberActivity memberActivity = MemberActivity.createMakersActivity(
            memberId,
            JobFamily.FE,
            RecruitTypeDetail.REGULAR,
            activityStatus,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO,
            "테스트 메모",
            MakersTeam.TEAM_1,
            Availability.HIGHLY_AVAILABLE,
            Availability.AVAILABLE_BY_TOPIC,
            Availability.CONSIDER_LATER,
            CareerLevel.JUNIOR,
            "Spring",
            "JECT",
            "백오피스",
            "MK-001"
        );
        return memberActivity;
    }

    private MemberActivity createSupportersActivity(Long memberId, ActivityStatus activityStatus) {
        return MemberActivity.createSupportersActivity(
            memberId,
            JobFamily.OPS,
            RecruitTypeDetail.REGULAR,
            activityStatus,
            null,
            null,
            "SP-001",
            "테스트 메모"
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

    @Test
    @DisplayName("메이커스팀으로 활동 중인 이력이 존재하면 true를 반환한다")
    void 메이커스팀으로_활동_중인_이력이_존재하면_true를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("makers@test.com").build());
        memberActivityRepository.saveAndFlush(createMakersActivity(member.getId(), ActivityStatus.ACTIVE));

        // when
        boolean result = memberActivityRepository.existsActiveMakersActivityByMemberId(member.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("메이커스팀으로 활동 중인 이력이 없으면 false를 반환한다")
    void 메이커스팀으로_활동_중인_이력이_없으면_false를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("completed-makers@test.com").build());
        memberActivityRepository.saveAndFlush(createMakersActivity(member.getId(), ActivityStatus.ENDED));

        // when
        boolean result = memberActivityRepository.existsActiveMakersActivityByMemberId(member.getId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("운영 서포터즈로 활동 중인 이력이 존재하면 true를 반환한다")
    void 운영_서포터즈로_활동_중인_이력이_존재하면_true를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("supporters@test.com").build());
        memberActivityRepository.saveAndFlush(createSupportersActivity(member.getId(), ActivityStatus.ACTIVE));

        // when
        boolean result = memberActivityRepository.existsActiveSupportersActivityByMemberId(member.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("운영 서포터즈 활동이 ACTIVE가 아니면 false를 반환한다")
    void 운영_서포터즈_활동이_ACTIVE가_아니면_false를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("ended-supporters@test.com").build());
        memberActivityRepository.saveAndFlush(createSupportersActivity(member.getId(), ActivityStatus.ENDED));

        // when
        boolean result = memberActivityRepository.existsActiveSupportersActivityByMemberId(member.getId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 유형의 ACTIVE 활동이면 운영 서포터즈 활동 조회에서 false를 반환한다")
    void 다른_유형의_ACTIVE_활동이면_운영_서포터즈_활동_조회에서_false를_반환한다() {
        // given
        Member member = memberRepository.save(member().email("active-makers@test.com").build());
        memberActivityRepository.saveAndFlush(createMakersActivity(member.getId(), ActivityStatus.ACTIVE));

        // when
        boolean result = memberActivityRepository.existsActiveSupportersActivityByMemberId(member.getId());

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
    @Test
    @DisplayName("메이커스팀 구성원 목록을 최신순으로 조회한다")
    void 메이커스팀_구성원_목록을_최신순으로_조회한다() {
        // given
        Member first = memberRepository.save(member().email("makers1@test.com").build());
        Member second = memberRepository.save(member().email("makers2@test.com").build());
        Member third = memberRepository.save(member().email("makers3@test.com").build());

        MemberActivity firstAcivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(first.getId()).build());
        MemberActivity secondAcivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(second.getId()).build());
        MemberActivity thirdAcivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(third.getId()).build());
        // when
        List<MemberMakersListProjection> projections = memberActivityRepository.findMemberMakersList(null,4);
        // then
        assertThat(projections).hasSize(3);
        assertThat(projections)
            .extracting(MemberMakersListProjection::memberActivityId)
            .containsExactly(
                thirdAcivity.getId(),
                secondAcivity.getId(),
                firstAcivity.getId()
            );
    }

    @Test
    @DisplayName("cursor 이후 메이커스팀 구성원 목록을 조회한다")
    void cursor_이후_메이커스팀_구성원_목록을_조회한다() {
        // given
        Member first = memberRepository.save(member().email("makers1@test.com").build());
        Member second = memberRepository.save(member().email("makers2@test.com").build());
        Member third = memberRepository.save(member().email("makers3@test.com").build());

        MemberActivity firstAcivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(first.getId()).build());
        MemberActivity secondAcivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(second.getId()).build());
        MemberActivity thirdAcivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(third.getId()).build());

        Long cursor = thirdAcivity.getId().longValue();
        // when
        List<MemberMakersListProjection> projections = memberActivityRepository.findMemberMakersList(cursor,4);
        // then
        assertThat(projections).hasSize(2);
        assertThat(projections)
            .extracting(MemberMakersListProjection::memberActivityId)
            .containsExactly(
                secondAcivity.getId(),
                firstAcivity.getId()
            );
    }

    @Test
    @DisplayName("메이커스팀 구성원 목록 전체 개수를 조회한다")
    void 메이커스팀_구성원_목록_전체_개수를_조회한다() {
        // given
        Member first = memberRepository.save(member().email("makers1@test.com").build());
        Member second = memberRepository.save(member().email("makers2@test.com").build());
        Member third = memberRepository.save(member().email("makers3@test.com").build());

        memberActivityRepository.saveAndFlush(makersActivity().memberId(first.getId()).build());
        memberActivityRepository.saveAndFlush(makersActivity().memberId(second.getId()).build());
        memberActivityRepository.saveAndFlush(makersActivity().memberId(third.getId()).build());
        // when
        long count = memberActivityRepository.countMemberMakersList();
        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("일반 구성원 활동은 메이커스팀 목록에서 제외한다")
    void 일반_구성원_활동은_메이커스팀_목록에서_제외한다() {
        // given
        Member first = memberRepository.save(member().email("makers1@test.com").build());
        Member second = memberRepository.save(member().email("makers2@test.com").build());
        Member semester = memberRepository.save(member().email("semester1@test.com").build());

        MemberActivity firstActivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(first.getId()).build());
        MemberActivity secondActivity = memberActivityRepository.saveAndFlush(makersActivity().memberId(second.getId()).build());
        MemberActivity semesterActivity = memberActivityRepository.saveAndFlush(semesterActivity().memberId(semester.getId()).build());
        // when
        List<MemberMakersListProjection> projections = memberActivityRepository.findMemberMakersList(null,4);
        // then
        assertThat(projections).hasSize(2);
        assertThat(projections)
            .extracting(MemberMakersListProjection::memberActivityId)
            .containsExactly(
                secondActivity.getId(),
                firstActivity.getId()
            );
    }

    @Test
    @DisplayName("운영 서포터즈 구성원 목록을 최신순으로 조회한다")
    void 운영_서포터즈_구성원_목록을_최신순으로_조회한다() {
        // given
        Member first = memberRepository.save(member().email("supporters1@test.com").build());
        Member second = memberRepository.save(member().email("supporters2@test.com").build());
        Member third = memberRepository.save(member().email("supporters3@test.com").build());
        MemberActivity firstActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(first.getId(), ActivityStatus.ACTIVE));
        MemberActivity secondActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(second.getId(), ActivityStatus.ENDED));
        MemberActivity thirdActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(third.getId(), ActivityStatus.ACTIVE));

        // when
        List<MemberSupportersListProjection> projections =
            memberActivityRepository.findMemberSupportersList(null, 3);

        // then
        assertThat(projections)
            .extracting(MemberSupportersListProjection::memberActivityId)
            .containsExactly(thirdActivity.getId(), secondActivity.getId(), firstActivity.getId());
    }

    @Test
    @DisplayName("cursor 이후 운영 서포터즈 구성원 목록을 조회한다")
    void cursor_이후_운영_서포터즈_구성원_목록을_조회한다() {
        // given
        Member first = memberRepository.save(member().email("supporters1@test.com").build());
        Member second = memberRepository.save(member().email("supporters2@test.com").build());
        Member third = memberRepository.save(member().email("supporters3@test.com").build());
        MemberActivity firstActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(first.getId(), ActivityStatus.ACTIVE));
        MemberActivity secondActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(second.getId(), ActivityStatus.ENDED));
        MemberActivity thirdActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(third.getId(), ActivityStatus.ACTIVE));

        // when
        List<MemberSupportersListProjection> projections =
            memberActivityRepository.findMemberSupportersList(thirdActivity.getId(), 3);

        // then
        assertThat(projections)
            .extracting(MemberSupportersListProjection::memberActivityId)
            .containsExactly(secondActivity.getId(), firstActivity.getId());
    }

    @Test
    @DisplayName("운영 서포터즈 목록과 전체 개수에서 다른 활동과 삭제 데이터를 제외한다")
    void 운영_서포터즈_목록과_전체_개수에서_다른_활동과_삭제_데이터를_제외한다() {
        // given
        Member supporters = memberRepository.save(member().email("supporters@test.com").build());
        Member deletedMember = memberRepository.save(member().email("deleted-member@test.com").deleted().build());
        Member deletedActivityMember = memberRepository.save(member().email("deleted-activity@test.com").build());
        Member makers = memberRepository.save(member().email("makers@test.com").build());
        MemberActivity supportersActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(supporters.getId(), ActivityStatus.ACTIVE));
        memberActivityRepository.saveAndFlush(
            createSupportersActivity(deletedMember.getId(), ActivityStatus.ACTIVE));
        MemberActivity deletedActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(deletedActivityMember.getId(), ActivityStatus.ACTIVE));
        memberActivityRepository.saveAndFlush(createMakersActivity(makers.getId(), ActivityStatus.ACTIVE));
        memberActivityRepository.delete(deletedActivity);
        entityManager.flush();
        entityManager.clear();

        // when
        List<MemberSupportersListProjection> projections =
            memberActivityRepository.findMemberSupportersList(null, 10);
        long totalCount = memberActivityRepository.countMemberSupportersList();

        // then
        assertThat(projections)
            .extracting(MemberSupportersListProjection::memberActivityId)
            .containsExactly(supportersActivity.getId());
        assertThat(totalCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("메이커스팀 구성원 상세를 조회한다")
    void 메이커스팀_구성원_상세를_조회한다() {
        // given
        Member member = memberRepository.save(member()
            .email("makers-detail@test.com")
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            makersActivity().memberId(member.getId()).build()
        );

        // when
        MemberMakersDetailProjection projection = memberActivityRepository.findMemberMakersDetail(memberActivity.getId())
            .orElseThrow();

        // then
        assertThat(projection.memberActivityId()).isEqualTo(memberActivity.getId());
        assertThat(projection.name()).isEqualTo(member.getName());
        assertThat(projection.email()).isEqualTo(member.getEmail());
        assertThat(projection.jobFamily()).isEqualTo(memberActivity.getJobFamily());
        assertThat(projection.makersTeam()).isEqualTo(memberActivity.getMemberMakers().getMakersTeam());
        assertThat(projection.activityStatus()).isEqualTo(memberActivity.getActivityStatus());
    }

    @Test
    @DisplayName("삭제된 메이커스팀 활동은 상세 조회하지 않는다")
    void 삭제된_메이커스팀_활동은_상세_조회하지_않는다() {
        // given
        Member member = memberRepository.save(member()
            .email("del-act@test.com")
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            makersActivity()
                .memberId(member.getId())
                .deleted()
                .build()
        );

        // when
        boolean exists = memberActivityRepository.findMemberMakersDetail(memberActivity.getId()).isPresent();

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("삭제된 구성원의 메이커스팀 활동은 상세 조회하지 않는다")
    void 삭제된_구성원의_메이커스팀_활동은_상세_조회하지_않는다() {
        // given
        Member member = memberRepository.save(member()
            .email("del-mem@test.com")
            .deleted()
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            makersActivity().memberId(member.getId()).build()
        );

        // when
        boolean exists = memberActivityRepository.findMemberMakersDetail(memberActivity.getId()).isPresent();

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("운영 서포터즈 구성원의 상세정보를 조회한다")
    void 운영_서포터즈_구성원의_상세정보를_조회한다() {
        // given
        Member member = memberRepository.save(member()
            .email("supporters-detail@test.com")
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(member.getId(), ActivityStatus.ACTIVE));

        // when
        MemberSupportersDetailProjection projection =
            memberActivityRepository.findMemberSupportersDetail(memberActivity.getId()).orElseThrow();

        // then
        assertThat(projection.memberActivityId()).isEqualTo(memberActivity.getId());
        assertThat(projection.name()).isEqualTo(member.getName());
        assertThat(projection.phoneNumber()).isEqualTo(member.getPhoneNumber());
        assertThat(projection.email()).isEqualTo(member.getEmail());
        assertThat(projection.memberType()).isEqualTo(MemberType.SUPPORTERS);
        assertThat(projection.jobFamily()).isEqualTo(memberActivity.getJobFamily());
        assertThat(projection.recruitTypeDetail()).isEqualTo(memberActivity.getRecruitTypeDetail());
        assertThat(projection.activityStatus()).isEqualTo(memberActivity.getActivityStatus());
        assertThat(projection.startDate()).isEqualTo(memberActivity.getStartDate());
        assertThat(projection.endDate()).isEqualTo(memberActivity.getEndDate());
        assertThat(projection.activityCertNumber())
            .isEqualTo(memberActivity.getMemberSupporters().getActivityCertNumber());
        assertThat(projection.memo()).isEqualTo(memberActivity.getMemo());
    }

    @Test
    @DisplayName("존재하지 않는 활동 이력을 조회하면 빈 결과를 반환한다")
    void 존재하지_않는_활동_이력을_조회하면_빈_결과를_반환한다() {
        assertThat(memberActivityRepository.findMemberSupportersDetail(999999999L)).isEmpty();
    }

    @Test
    @DisplayName("삭제된 구성원은 상세조회에서 제외한다")
    void 삭제된_구성원은_상세조회에서_제외한다() {
        // given
        Member member = memberRepository.save(member()
            .email("deleted-supporters@test.com")
            .deleted()
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(member.getId(), ActivityStatus.ACTIVE));

        // when & then
        assertThat(memberActivityRepository.findMemberSupportersDetail(memberActivity.getId())).isEmpty();
    }

    @Test
    @DisplayName("삭제된 활동 이력은 상세조회에서 제외한다")
    void 삭제된_활동_이력은_상세조회에서_제외한다() {
        // given
        Member member = memberRepository.save(member()
            .email("deleted-activity@test.com")
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            createSupportersActivity(member.getId(), ActivityStatus.ACTIVE));
        memberActivityRepository.delete(memberActivity);
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThat(memberActivityRepository.findMemberSupportersDetail(memberActivity.getId())).isEmpty();
    }

    @Test
    @DisplayName("다른 유형의 구성원은 운영 서포터즈 상세조회에서 제외한다")
    void 다른_유형의_구성원은_운영_서포터즈_상세조회에서_제외한다() {
        // given
        Member member = memberRepository.save(member()
            .email("makers-not-supporters@test.com")
            .build());
        MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
            makersActivity().memberId(member.getId()).build());

        // when & then
        assertThat(memberActivityRepository.findMemberSupportersDetail(memberActivity.getId())).isEmpty();
    }

	@Test
	@DisplayName("ID와 유형이 일치하는 구성원 활동을 조회한다")
	void ID와_유형이_일치하는_구성원_활동을_조회한다() {
		// given
		Member member = memberRepository.save(member().email("makers-type@test.com").build());
		MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
			makersActivity().memberId(member.getId()).build()
		);

		// when & then
		assertThat(memberActivityRepository.findByIdAndMemberType(memberActivity.getId(), MemberType.MAKERS))
			.contains(memberActivity);
	}

	@Test
	@DisplayName("ID가 같아도 유형이 다르면 조회하지 않는다")
	void ID가_같아도_유형이_다르면_조회하지_않는다() {
		// given
		Member member = memberRepository.save(member().email("semester-type@test.com").build());
		MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
			semesterActivity().memberId(member.getId()).build()
		);

		// when & then
		assertThat(memberActivityRepository.findByIdAndMemberType(memberActivity.getId(), MemberType.MAKERS))
			.isEmpty();
	}

	@Test
	@DisplayName("선택한 ID에 해당하는 메이커스팀 활동만 조회한다")
	void 선택한_ID에_해당하는_메이커스팀_활동만_조회한다() {
		// given
		Member makersMember = memberRepository.save(member().email("makers-list@test.com").build());
		Member semesterMember = memberRepository.save(member().email("semester-list@test.com").build());
		MemberActivity makers = memberActivityRepository.saveAndFlush(
			makersActivity().memberId(makersMember.getId()).build()
		);
		MemberActivity semester = memberActivityRepository.saveAndFlush(
			semesterActivity().memberId(semesterMember.getId()).build()
		);

		// when
		List<MemberActivity> result = memberActivityRepository.findAllByIdInAndMemberType(
			Set.of(makers.getId(), semester.getId()),
			MemberType.MAKERS
		);

		// then
		assertThat(result).containsExactly(makers);
	}

	@Test
	@DisplayName("구성원에게 활동이 남아 있으면 true를 반환한다")
	void 구성원에게_활동이_남아_있으면_true를_반환한다() {
		// given
		Member member = memberRepository.save(member().email("activity-exists@test.com").build());
		memberActivityRepository.saveAndFlush(makersActivity().memberId(member.getId()).build());

		// when & then
		assertThat(memberActivityRepository.existsByMemberId(member.getId())).isTrue();
	}

	@Test
	@DisplayName("삭제된 활동만 있으면 false를 반환한다")
	void 삭제된_활동만_있으면_false를_반환한다() {
		// given
		Member member = memberRepository.save(member().email("activity-deleted@test.com").build());
		MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
			makersActivity().memberId(member.getId()).build()
		);
		memberActivityRepository.delete(memberActivity);
		memberActivityRepository.flush();
		entityManager.clear();

		// when & then
		assertThat(memberActivityRepository.existsByMemberId(member.getId())).isFalse();
	}

	@Test
	@DisplayName("활동이 남아 있는 구성원 ID만 조회한다")
	void 활동이_남아_있는_구성원_ID만_조회한다() {
		// given
		Member activeMember = memberRepository.save(member().email("active-member@test.com").build());
		Member inactiveMember = memberRepository.save(member().email("inactive-member@test.com").build());
		memberActivityRepository.saveAndFlush(makersActivity().memberId(activeMember.getId()).build());

		// when
		Set<Long> memberIds = memberActivityRepository.findMemberIdsWithActivity(
			Set.of(activeMember.getId(), inactiveMember.getId())
		);

		// then
		assertThat(memberIds).containsExactly(activeMember.getId());
	}

	@Test
	@DisplayName("삭제된 활동의 구성원 ID는 조회하지 않는다")
	void 삭제된_활동의_구성원_ID는_조회하지_않는다() {
		// given
		Member member = memberRepository.save(member().email("deleted-member-id@test.com").build());
		MemberActivity memberActivity = memberActivityRepository.saveAndFlush(
			makersActivity().memberId(member.getId()).build()
		);
		memberActivityRepository.delete(memberActivity);
		memberActivityRepository.flush();
		entityManager.clear();

		// when & then
		assertThat(memberActivityRepository.findMemberIdsWithActivity(Set.of(member.getId()))).isEmpty();
	}
}
