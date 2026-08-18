package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import java.util.Set;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.request.DeleteMembersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.response.MemberMakersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberMakersListResponse;
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
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberMakersUseCaseTest {

	@Mock
	private AdminMemberService adminMemberService;

	@Mock
	private AdminMemberActivityService adminMemberActivityService;

	@InjectMocks
	private AdminMemberMakersUseCase adminMemberMakersUseCase;

	@Test
	@DisplayName("조회 결과가 size보다 많으면 다음 커서를 반환한다")
	void 조회_결과가_size보다_많으면_다음_커서를_반환한다() {
		// given
		MemberMakersListRequest request = new MemberMakersListRequest(null, 2);
		MemberPageResult<MemberMakersListProjection> pageResult = MemberPageResult.of(
			List.of(
				makersProjection(5L, "첫번째"),
				makersProjection(4L, "두번째"),
				makersProjection(3L, "세번째")
			),
			3L
		);
		given(adminMemberActivityService.getMemberMakersList(request)).willReturn(pageResult);

		// when
		CursorPageResponse<MemberMakersListResponse> response =
			adminMemberMakersUseCase.getMemberMakersList(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.content())
			.extracting(MemberMakersListResponse::memberActivityId)
			.containsExactly(5L, 4L);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.nextCursor()).isEqualTo(4L);
		assertThat(response.totalCount()).isEqualTo(3L);
		verify(adminMemberActivityService).getMemberMakersList(request);
	}

	@Test
	@DisplayName("조회 결과가 size보다 적으면 다음 커서를 반환하지 않는다")
	void 조회_결과가_size보다_적으면_다음_커서를_반환하지_않는다() {
		// given
		MemberMakersListRequest request = new MemberMakersListRequest(4L, 3);
		MemberPageResult<MemberMakersListProjection> pageResult = MemberPageResult.of(
			List.of(
				makersProjection(3L, "첫번째"),
				makersProjection(2L, "두번째")
			),
			2L
		);
		given(adminMemberActivityService.getMemberMakersList(request)).willReturn(pageResult);

		// when
		CursorPageResponse<MemberMakersListResponse> response =
			adminMemberMakersUseCase.getMemberMakersList(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.totalCount()).isEqualTo(2L);
	}

	@Test
	@DisplayName("메이커스팀 구성원 상세를 조회해 응답으로 가공 후 반환한다")
	void 메이커스팀_구성원_상세를_조회해_응답으로_가궁_후_변환한다() {
		// given
		Long memberActivityId = 1L;
		MemberMakersDetailProjection projection = makersDetailProjection(memberActivityId);
		given(adminMemberActivityService.getMemberMakersDetail(memberActivityId)).willReturn(projection);

		// when
		MemberMakersDetailResponse response = adminMemberMakersUseCase.getMemberMakersDetail(memberActivityId);

		// then
		assertThat(response.memberActivityId()).isEqualTo(projection.memberActivityId());
		assertThat(response.name()).isEqualTo(projection.name());
		assertThat(response.email()).isEqualTo(projection.email());
		assertThat(response.makersTeam()).isEqualTo(projection.makersTeam());
		assertThat(response.activityStatus()).isEqualTo(projection.activityStatus());
		verify(adminMemberActivityService).getMemberMakersDetail(memberActivityId);
	}

	@Test
	@DisplayName("메이커스팀 구성원을 삭제한다")
	void 메이커스팀_구성원을_삭제한다() {
		// given
		Long memberActivityId = 1L;
		Long memberId = 10L;
		given(adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.MAKERS)).willReturn(memberId);

		// when
		adminMemberMakersUseCase.deleteMemberMakers(memberActivityId);

		// then
		verify(adminMemberService).deleteMemberIfNoActivity(memberId);
	}

	@Test
	@DisplayName("선택한 메이커스팀 구성원을 모두 삭제한다")
	void 선택한_메이커스팀_구성원을_모두_삭제한다() {
		// given
		DeleteMembersRequest request = new DeleteMembersRequest(Set.of(1L, 2L));
		Set<Long> memberIds = Set.of(10L, 20L);
		given(adminMemberActivityService.deleteMemberActivities(request.memberActivityIds(), MemberType.MAKERS))
			.willReturn(memberIds);

		// when
		adminMemberMakersUseCase.deleteMemberMakersList(request);

		// then
		verify(adminMemberService).deleteMembersIfNoActivity(memberIds);
	}

	private MemberMakersListProjection makersProjection(Long memberActivityId, String name) {
		return new MemberMakersListProjection(
			memberActivityId,
			name,
			name + "@ject.kr",
			"01012345678",
			JobFamily.FE,
			MakersTeam.TEAM_1,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ACTIVE,
			"memo"
		);
	}

	private MemberMakersDetailProjection makersDetailProjection(Long memberActivityId) {
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
}
