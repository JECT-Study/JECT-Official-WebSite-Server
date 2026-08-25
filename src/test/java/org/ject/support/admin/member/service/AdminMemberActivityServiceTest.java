package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberActivityServiceTest {

    @Mock
    private MemberActivityRepository memberActivityRepository;

    @InjectMocks
    private AdminMemberActivityService adminMemberActivityService;

    @Test
    @DisplayName("운영 서포터즈 목록을 size보다 한 건 더 조회하고 전체 개수를 반환한다")
    void 운영_서포터즈_목록을_size보다_한_건_더_조회하고_전체_개수를_반환한다() {
        // given
        MemberSupportersListRequest request = new MemberSupportersListRequest(10L, 2);
        List<MemberSupportersListProjection> projections = List.of(
            supportersProjection(9L),
            supportersProjection(8L),
            supportersProjection(7L)
        );
        given(memberActivityRepository.findMemberSupportersList(10L, 3)).willReturn(projections);
        given(memberActivityRepository.countMemberSupportersList()).willReturn(5L);

        // when
        MemberPageResult<MemberSupportersListProjection> result =
            adminMemberActivityService.getMemberSupportersList(request);

        // then
        assertThat(result.content()).isEqualTo(projections);
        assertThat(result.totalCount()).isEqualTo(5L);
        verify(memberActivityRepository).findMemberSupportersList(10L, 3);
        verify(memberActivityRepository).countMemberSupportersList();
    }

    /**
     * 일반 구성원 추가 테스트
     */
    @Test
    @DisplayName("동일 기수 활동이 없으면 일반 구성원 활동을 저장한다")
    void 동일_기수_활동이_없으면_일반_구성원_활동을_저장한다() {
        // given
        CreateMemberSemesterRequest request = createMemberSemesterRequest();
        Long memberId = 1L;
        given(memberActivityRepository.existsSemesterActivity(
            memberId,
            MemberType.SEMESTER,
            request.semesterId()
        )).willReturn(false);

        // when
        adminMemberActivityService.createMemberSemesterActivity(request, memberId);

        // then
        ArgumentCaptor<MemberActivity> captor = ArgumentCaptor.forClass(MemberActivity.class);
        verify(memberActivityRepository).save(captor.capture());

        MemberActivity memberActivity = captor.getValue();
        assertThat(memberActivity.getMemberId()).isEqualTo(memberId);
        assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
        assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
        assertThat(memberActivity.getCareerDetails()).isEqualTo(request.careerDetails());
        assertThat(memberActivity.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
        assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
        assertThat(memberActivity.getMemberSemester().getSemesterId()).isEqualTo(request.semesterId());
        assertThat(memberActivity.getMemberSemester().getTeamId()).isEqualTo(request.teamId());
    }

    @Test
    @DisplayName("동일 기수 활동이 있으면 예외가 발생한다")
    void 동일_기수_활동이_있으면_예외가_발생한다() {
        // given
        CreateMemberSemesterRequest request = createMemberSemesterRequest();
        Long memberId = 1L;
        given(memberActivityRepository.existsSemesterActivity(
            memberId,
            MemberType.SEMESTER,
            request.semesterId()
        )).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberSemesterActivity(request, memberId)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    @Test
    @DisplayName("활동 종료 상태로 메이커스팀을 추가하면 기존 활동 중 이력을 조회하지 않고 저장한다")
    void 활동_종료_상태로_메이커스팀을_추가하면_기존_활동_중_이력을_조회하지_않고_저장한다() {
        // given
        CreateMemberMakersRequest request = createMemberMakersRequest();
        Long memberId = 1L;

        // when
        adminMemberActivityService.createMemberMakersActivity(request, memberId);

        // then
        ArgumentCaptor<MemberActivity> captor = ArgumentCaptor.forClass(MemberActivity.class);
        verify(memberActivityRepository).save(captor.capture());

        MemberActivity memberActivity = captor.getValue();
        assertThat(memberActivity.getMemberId()).isEqualTo(memberId);
        assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.MAKERS);
        assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
        assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
        assertThat(memberActivity.getCareerDetails()).isEqualTo(request.careerDetails());
        assertThat(memberActivity.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
        assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
        assertThat(memberActivity.getMemberMakers().getMakersTeam()).isEqualTo(request.makersTeam());
        assertThat(memberActivity.getMemberMakers().getMentoringAvailability()).isEqualTo(request.mentoringAvailability());
        assertThat(memberActivity.getMemberMakers().getProjectSupplementAvailability()).isEqualTo(request.projectSupplementAvailability());
        assertThat(memberActivity.getMemberMakers().getSpeakerAvailability()).isEqualTo(request.speakerAvailability());
        assertThat(memberActivity.getMemberMakers().getCareerLevel()).isEqualTo(request.careerLevel());
        assertThat(memberActivity.getMemberMakers().getSkills()).isEqualTo(request.skills());
        assertThat(memberActivity.getMemberMakers().getCompany()).isEqualTo(request.company());
        assertThat(memberActivity.getMemberMakers().getExpertTopics()).isEqualTo(request.expertTopics());
        assertThat(memberActivity.getMemberMakers().getActivityCertNumber()).isEqualTo(request.activityCertNumber());
        verify(memberActivityRepository, never()).existsActiveMakersActivityByMemberId(memberId);
    }

    @Test
    @DisplayName("활동 중 상태로 메이커스팀을 추가할 때 기존 활동 중 이력이 있으면 예외가 발생한다")
    void 활동_중_상태로_메이커스팀을_추가할_때_기존_활동_중_이력이_있으면_예외가_발생한다() {
        // given
        CreateMemberMakersRequest request = createMemberMakersRequest(ActivityStatus.ACTIVE);
        Long memberId = 1L;
        given(memberActivityRepository.existsActiveMakersActivityByMemberId(memberId)).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberMakersActivity(request, memberId)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.ALREADY_EXIST_ACTIVE_MEMBER_MAKERS_ACTIVITY);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    @Test
    @DisplayName("운영 서포터즈로 활동 중인 이력이 없으면 신규 운영 서포터즈 활동 이력을 생성한다")
    void 운영_서포터즈로_활동_중인_이력이_없으면_신규_운영_서포터즈_활동_이력을_생성한다() {
        // given
        CreateMemberSupportersRequest request = createMemberSupportersRequest();
        Long memberId = 1L;

        // when
        adminMemberActivityService.createMemberSupportersActivity(request, memberId);

        // then
        ArgumentCaptor<MemberActivity> captor = ArgumentCaptor.forClass(MemberActivity.class);
        verify(memberActivityRepository).save(captor.capture());

        MemberActivity memberActivity = captor.getValue();
        assertThat(memberActivity.getMemberId()).isEqualTo(memberId);
        assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.SUPPORTERS);
        assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
        assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
        assertThat(memberActivity.getStartDate()).isEqualTo(request.startDate());
        assertThat(memberActivity.getEndDate()).isEqualTo(request.endDate());
        assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
        assertThat(memberActivity.getMemberSupporters().getActivityCertNumber()).isEqualTo(request.activityCertNumber());
        verify(memberActivityRepository, never()).existsActiveSupportersActivityByMemberId(memberId);
    }

    @Test
    @DisplayName("활동 중 상태로 운영 서포터즈를 추가할 때 기존 활동 중 이력이 있으면 예외가 발생한다")
    void 활동_중_상태로_운영_서포터즈를_추가할_때_기존_활동_중_이력이_있으면_예외가_발생한다() {
        // given
        CreateMemberSupportersRequest original = createMemberSupportersRequest();
        CreateMemberSupportersRequest request = new CreateMemberSupportersRequest(
            original.name(),
            original.phoneNumber(),
            original.email(),
            original.memberType(),
            original.jobFamily(),
            original.recruitTypeDetail(),
            ActivityStatus.ACTIVE,
            original.startDate(),
            original.endDate(),
            original.activityCertNumber(),
            original.memo()
        );
        Long memberId = 1L;
        given(memberActivityRepository.existsActiveSupportersActivityByMemberId(memberId)).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberSupportersActivity(request, memberId)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.ALREADY_EXIST_ACTIVE_MEMBER_SUPPORTERS_ACTIVITY);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    /**
     * 일반 구성원 목록 조회 테스트
     */

    @Test
    @DisplayName("단일 필터를 적용해서 일반 구성원 목록을 조회한다")
    void 단일_필터를_적용해서_일반_구성원_목록을_조회한다() {
        // given
        MemberSemesterSearchCondition condition = new MemberSemesterSearchCondition(
          null,
          20,
          1L,
          JobFamily.BE,
          RecruitTypeDetail.REGULAR,
          CareerDetails.EMPLOYEE,
          1L,
          ActivityStatus.ACTIVE
        );

        List<SearchMemberSemesterProjection> projections = List.of(
            new SearchMemberSemesterProjection(
                1L,
                "김젝트",
                JobFamily.BE,
                "01012345678",
                CareerDetails.EMPLOYEE,
                ExperiencePeriod.ONE_TO_TWO,
                ActivityStatus.ACTIVE
            )
        );
        given(memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1)).willReturn(projections);
        given(memberActivityRepository.countMemberSemesters(condition)).willReturn(1L);

        // when
        MemberPageResult<SearchMemberSemesterProjection> pageResult =
            adminMemberActivityService.searchMemberSemesterList(condition);
        // then
        assertThat(pageResult.content()).isEqualTo(projections);
        assertThat(pageResult.totalCount()).isEqualTo(1L);
        verify(memberActivityRepository).searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        verify(memberActivityRepository).countMemberSemesters(condition);
    }

    @Test
    @DisplayName("메이커스팀 구성원 상세를 조회한다")
    void 메이커스팀_구성원_상세를_조회한다() {
        // given
        Long memberActivityId = 1L;
        MemberMakersDetailProjection projection = memberMakersDetailProjection(memberActivityId);
        given(memberActivityRepository.findMemberMakersDetail(memberActivityId)).willReturn(Optional.of(projection));

        // when
        MemberMakersDetailProjection result = adminMemberActivityService.getMemberMakersDetail(memberActivityId);

        // then
        assertThat(result).isEqualTo(projection);
        verify(memberActivityRepository).findMemberMakersDetail(memberActivityId);
    }

    @Test
    @DisplayName("메이커스팀 구성원 상세가 없으면 예외가 발생한다")
    void 메이커스팀_구성원_상세가_없으면_예외가_발생한다() {
        // given
        Long memberActivityId = 1L;
        given(memberActivityRepository.findMemberMakersDetail(memberActivityId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> adminMemberActivityService.getMemberMakersDetail(memberActivityId));

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
        verify(memberActivityRepository).findMemberMakersDetail(memberActivityId);
    }

    @Test
    @DisplayName("운영 서포터즈 구성원의 상세정보를 조회한다")
    void 운영_서포터즈_구성원의_상세정보를_조회한다() {
        // given
        Long memberActivityId = 1L;
        MemberSupportersDetailProjection projection = memberSupportersDetailProjection(memberActivityId);
        given(memberActivityRepository.findMemberSupportersDetail(memberActivityId))
            .willReturn(Optional.of(projection));

        // when
        MemberSupportersDetailProjection result =
            adminMemberActivityService.getMemberSupportersDetail(memberActivityId);

        // then
        assertThat(result).isEqualTo(projection);
        verify(memberActivityRepository).findMemberSupportersDetail(memberActivityId);
    }

    @Test
    @DisplayName("운영 서포터즈 구성원을 찾을 수 없으면 예외가 발생한다")
	void 운영_서포터즈_구성원을_찾을_수_없으면_예외가_발생한다() {
        // given
        Long memberActivityId = 1L;
        given(memberActivityRepository.findMemberSupportersDetail(memberActivityId))
            .willReturn(Optional.empty());

        // when
        Throwable throwable =
            catchThrowable(() -> adminMemberActivityService.getMemberSupportersDetail(memberActivityId));

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
		verify(memberActivityRepository).findMemberSupportersDetail(memberActivityId);
	}

	@Test
	@DisplayName("메이커스팀 활동이 존재하면 삭제된다")
	void 메이커스팀_활동이_존재하면_삭제된다() {
		// given
		Long memberActivityId = 1L;
		MemberActivity memberActivity = mock(MemberActivity.class);
		given(memberActivity.getMemberId()).willReturn(10L);
		given(memberActivityRepository.findByIdAndMemberType(memberActivityId, MemberType.MAKERS))
			.willReturn(Optional.of(memberActivity));

		// when
		Long memberId = adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.MAKERS);

		// then
		assertThat(memberId).isEqualTo(10L);
		verify(memberActivityRepository).delete(memberActivity);
	}

	@Test
	@DisplayName("존재하지 않는 활동은 삭제하지 않는다")
	void 존재하지_않는_활동은_삭제하지_않는다() {
		// given
		Long memberActivityId = 1L;
		given(memberActivityRepository.findByIdAndMemberType(memberActivityId, MemberType.MAKERS))
			.willReturn(Optional.empty());

		// when
		Throwable throwable = catchThrowable(() ->
			adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.MAKERS)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER_MAKERS_ACTIVITY);
		verify(memberActivityRepository, never()).delete(any(MemberActivity.class));
	}

	@Test
	@DisplayName("존재하지 않는 일반 구성원 활동은 삭제하지 않는다")
	void 존재하지_않는_일반_구성원_활동은_삭제하지_않는다() {
		// given
		Long memberActivityId = 1L;
		given(memberActivityRepository.findByIdAndMemberType(memberActivityId, MemberType.SEMESTER))
			.willReturn(Optional.empty());

		// when
		Throwable throwable = catchThrowable(() ->
			adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.SEMESTER)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER_SEMESTER_ACTIVITY);
		verify(memberActivityRepository, never()).delete(any(MemberActivity.class));
	}

	@Test
	@DisplayName("존재하지 않는 운영 서포터즈 활동은 삭제하지 않는다")
	void 존재하지_않는_운영_서포터즈_활동은_삭제하지_않는다() {
		// given
		Long memberActivityId = 1L;
		given(memberActivityRepository.findByIdAndMemberType(memberActivityId, MemberType.SUPPORTERS))
			.willReturn(Optional.empty());

		// when
		Throwable throwable = catchThrowable(() ->
			adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.SUPPORTERS)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER_SUPPORTERS_ACTIVITY);
		verify(memberActivityRepository, never()).delete(any(MemberActivity.class));
	}

	@Test
	@DisplayName("운영 서포터즈 구성원을 삭제한다")
	void 운영_서포터즈_구성원을_삭제한다() {
		// given
		Long memberActivityId = 1L;
		MemberActivity memberActivity = mock(MemberActivity.class);
		given(memberActivity.getMemberId()).willReturn(10L);
		given(memberActivityRepository.findByIdAndMemberType(memberActivityId, MemberType.SUPPORTERS))
			.willReturn(Optional.of(memberActivity));

		// when
		Long memberId = adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.SUPPORTERS);

		// then
		assertThat(memberId).isEqualTo(10L);
		verify(memberActivityRepository).delete(memberActivity);
	}

	@Test
	@DisplayName("선택한 운영 서포터즈 구성원을 모두 삭제한다")
	void 선택한_운영_서포터즈_구성원을_모두_삭제한다() {
		// given
		Set<Long> memberActivityIds = Set.of(1L, 2L);
		MemberActivity first = memberActivity(1L, 10L);
		MemberActivity second = memberActivity(2L, 20L);
		List<MemberActivity> memberActivities = List.of(first, second);
		given(memberActivityRepository.findAllByIdInAndMemberType(memberActivityIds, MemberType.SUPPORTERS))
			.willReturn(memberActivities);

		// when
		Set<Long> memberIds = adminMemberActivityService.deleteMemberActivities(
			memberActivityIds,
			MemberType.SUPPORTERS
		);

		// then
		assertThat(memberIds).containsExactlyInAnyOrder(10L, 20L);
		verify(memberActivityRepository).deleteAll(memberActivities);
	}

	@Test
	@DisplayName("유효하지 않은 구성원이 포함되면 모든 운영 서포터즈 구성원을 유지한다")
	void 유효하지_않은_구성원이_포함되면_모든_운영_서포터즈_구성원을_유지한다() {
		// given
		Set<Long> memberActivityIds = Set.of(1L, 2L);
		MemberActivity memberActivity = memberActivity(1L);
		given(memberActivityRepository.findAllByIdInAndMemberType(memberActivityIds, MemberType.SUPPORTERS))
			.willReturn(List.of(memberActivity));

		// when
		Throwable throwable = catchThrowable(() ->
			adminMemberActivityService.deleteMemberActivities(memberActivityIds, MemberType.SUPPORTERS)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER_SUPPORTERS_ACTIVITY);
		verify(memberActivityRepository, never()).deleteAll(any());
	}

	@Test
	@DisplayName("선택한 메이커스팀 활동을 모두 삭제한다")
	void 선택한_메이커스팀_활동을_모두_삭제한다() {
		// given
		Set<Long> memberActivityIds = Set.of(1L, 2L);
		MemberActivity first = memberActivity(1L, 10L);
		MemberActivity second = memberActivity(2L, 20L);
		List<MemberActivity> memberActivities = List.of(first, second);
		given(memberActivityRepository.findAllByIdInAndMemberType(memberActivityIds, MemberType.MAKERS))
			.willReturn(memberActivities);

		// when
		Set<Long> memberIds = adminMemberActivityService.deleteMemberActivities(memberActivityIds, MemberType.MAKERS);

		// then
		assertThat(memberIds).containsExactlyInAnyOrder(10L, 20L);
		verify(memberActivityRepository).deleteAll(memberActivities);
	}

	@Test
	@DisplayName("존재하지 않는 활동이 포함되면 모든 활동을 유지한다")
	void 존재하지_않는_활동이_포함되면_모든_활동을_유지한다() {
		// given
		Set<Long> memberActivityIds = Set.of(1L, 2L);
		MemberActivity memberActivity = memberActivity(1L);
		given(memberActivityRepository.findAllByIdInAndMemberType(memberActivityIds, MemberType.MAKERS))
			.willReturn(List.of(memberActivity));

		// when
		Throwable throwable = catchThrowable(() ->
			adminMemberActivityService.deleteMemberActivities(memberActivityIds, MemberType.MAKERS)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER_MAKERS_ACTIVITY);
		verify(memberActivityRepository, never()).deleteAll(any());
	}

	private MemberActivity memberActivity(Long memberActivityId) {
		MemberActivity memberActivity = mock(MemberActivity.class);
		given(memberActivity.getId()).willReturn(memberActivityId);
		return memberActivity;
	}

	private MemberActivity memberActivity(Long memberActivityId, Long memberId) {
		MemberActivity memberActivity = memberActivity(memberActivityId);
		given(memberActivity.getMemberId()).willReturn(memberId);
		return memberActivity;
	}

	private CreateMemberSemesterRequest createMemberSemesterRequest() {
        return new CreateMemberSemesterRequest(
            "김젝트",
            "jectkim@ject.kr",
            "01012345678",
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            ActivityStatus.COMPLETED,
            CareerDetails.EMPLOYEE,
            1L,
            2L,
            ExperiencePeriod.ONE_TO_TWO,
            "memo",
            List.of("HEALTHCARE", "FINTECH", "AI"),
            Region.SEOUL
        );
    }

    private CreateMemberMakersRequest createMemberMakersRequest() {
        return createMemberMakersRequest(ActivityStatus.ENDED);
    }

    private CreateMemberMakersRequest createMemberMakersRequest(ActivityStatus activityStatus) {
        return new CreateMemberMakersRequest(
            "김메이커",
            "maker@ject.kr",
            "01087654321",
            JobFamily.FE,
            CareerDetails.EMPLOYEE,
            MakersTeam.TEAM_1,
            RecruitTypeDetail.REGULAR,
            activityStatus,
            Region.SEOUL,
            List.of("HEALTHCARE", "FINTECH", "AI"),
            ExperiencePeriod.ONE_TO_TWO,
            Availability.HIGHLY_AVAILABLE,
            Availability.AVAILABLE_BY_TOPIC,
            Availability.CONSIDER_LATER,
            CareerLevel.JUNIOR,
            "Spring",
            "JECT",
            "백오피스",
            "MK-001",
            "memo"
        );
    }

    private CreateMemberSupportersRequest createMemberSupportersRequest() {
        return new CreateMemberSupportersRequest(
            "김서포터",
            "01012341234",
            "supporter@ject.kr",
            MemberType.SUPPORTERS,
            JobFamily.OPS,
            RecruitTypeDetail.REGULAR,
            ActivityStatus.ENDED,
            java.time.LocalDate.of(2025, 5, 19),
            java.time.LocalDate.of(2025, 12, 19),
            "SP-001",
            "memo"
        );
    }

    private MemberMakersDetailProjection memberMakersDetailProjection(Long memberActivityId) {
        return new MemberMakersDetailProjection(
            memberActivityId,
            "김메이커",
            "maker@ject.kr",
            "01087654321",
            JobFamily.FE,
            CareerDetails.EMPLOYEE,
            MakersTeam.TEAM_1,
            RecruitTypeDetail.REGULAR,
            Region.SEOUL,
            List.of("HEALTHCARE"),
            ExperiencePeriod.ONE_TO_TWO,
            Availability.HIGHLY_AVAILABLE,
            Availability.AVAILABLE_BY_TOPIC,
            Availability.CONSIDER_LATER,
            CareerLevel.JUNIOR,
            ActivityStatus.ACTIVE,
            "Spring",
            "JECT",
            "백오피스",
            "MK-001",
            "memo"
        );
    }

    private MemberSupportersDetailProjection memberSupportersDetailProjection(Long memberActivityId) {
        return new MemberSupportersDetailProjection(
            memberActivityId,
            "김서포터",
            "01012341234",
            "supporter@ject.kr",
            MemberType.SUPPORTERS,
            JobFamily.OPS,
            RecruitTypeDetail.REGULAR,
            ActivityStatus.ACTIVE,
            java.time.LocalDate.of(2025, 5, 19),
            java.time.LocalDate.of(2025, 12, 19),
            "SP-001",
            "memo"
        );
    }

    private MemberSupportersListProjection supportersProjection(Long memberActivityId) {
        return new MemberSupportersListProjection(
            memberActivityId,
            "김서포터",
            "01012341234",
            JobFamily.OPS,
            RecruitTypeDetail.REGULAR,
            ActivityStatus.ACTIVE,
            "memo"
        );
    }

}
