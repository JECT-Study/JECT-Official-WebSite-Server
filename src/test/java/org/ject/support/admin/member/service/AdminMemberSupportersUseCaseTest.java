package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import java.util.Set;

import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.request.DeleteMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.response.MemberSupportersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberSupportersListResponse;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberSupportersUseCaseTest {

	@Mock
	private AdminMemberService adminMemberService;

	@Mock
	private AdminMemberActivityService adminMemberActivityService;

	@InjectMocks
	private AdminMemberSupportersUseCase adminMemberSupportersUseCase;

	@Test
	@DisplayName("조회 결과가 size보다 많으면 다음 커서를 반환한다")
	void 조회_결과가_size보다_많으면_다음_커서를_반환한다() {
		// given
		MemberSupportersListRequest request = new MemberSupportersListRequest(null, 2);
		MemberPageResult<MemberSupportersListProjection> pageResult = MemberPageResult.of(
			List.of(
				supportersProjection(5L, "첫번째"),
				supportersProjection(4L, "두번째"),
				supportersProjection(3L, "세번째")
			),
			3L
		);
		given(adminMemberActivityService.getMemberSupportersList(request)).willReturn(pageResult);

		// when
		CursorPageResponse<MemberSupportersListResponse> response =
			adminMemberSupportersUseCase.getMemberSupportersList(request);

		// then
		assertThat(response.content())
			.extracting(MemberSupportersListResponse::memberActivityId)
			.containsExactly(5L, 4L);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.nextCursor()).isEqualTo(4L);
		assertThat(response.totalCount()).isEqualTo(3L);
		verify(adminMemberActivityService).getMemberSupportersList(request);
	}

	@Test
	@DisplayName("조회 결과가 size 이하면 다음 커서를 반환하지 않는다")
	void 조회_결과가_size_이하면_다음_커서를_반환하지_않는다() {
		// given
		MemberSupportersListRequest request = new MemberSupportersListRequest(4L, 3);
		MemberPageResult<MemberSupportersListProjection> pageResult = MemberPageResult.of(
			List.of(
				supportersProjection(3L, "첫번째"),
				supportersProjection(2L, "두번째")
			),
			2L
		);
		given(adminMemberActivityService.getMemberSupportersList(request)).willReturn(pageResult);

		// when
		CursorPageResponse<MemberSupportersListResponse> response =
			adminMemberSupportersUseCase.getMemberSupportersList(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.totalCount()).isEqualTo(2L);
	}

	@Test
	@DisplayName("운영 서포터즈 구성원의 상세정보를 응답으로 변환한다")
	void 운영_서포터즈_구성원의_상세정보를_응답으로_변환한다() {
		// given
		Long memberActivityId = 1L;
		MemberSupportersDetailProjection projection = supportersDetailProjection(memberActivityId);
		given(adminMemberActivityService.getMemberSupportersDetail(memberActivityId)).willReturn(projection);

		// when
		MemberSupportersDetailResponse response =
			adminMemberSupportersUseCase.getMemberSupportersDetail(memberActivityId);

		// then
		assertThat(response).isEqualTo(MemberSupportersDetailResponse.from(projection));
		verify(adminMemberActivityService).getMemberSupportersDetail(memberActivityId);
	}

	@Test
	@DisplayName("운영 서포터즈 구성원을 삭제하고 남은 활동이 없으면 구성원도 삭제한다")
	void 운영_서포터즈_구성원을_삭제하고_남은_활동이_없으면_구성원도_삭제한다() {
		// given
		Long memberActivityId = 1L;
		Long memberId = 10L;
		given(adminMemberActivityService.deleteMemberActivity(memberActivityId, MemberType.SUPPORTERS))
			.willReturn(memberId);

		// when
		adminMemberSupportersUseCase.deleteMemberSupporters(memberActivityId);

		// then
		verify(adminMemberService).deleteMemberIfNoActivity(memberId);
	}

	@Test
	@DisplayName("선택한 운영 서포터즈 구성원을 모두 삭제한다")
	void 선택한_운영_서포터즈_구성원을_모두_삭제한다() {
		// given
		DeleteMemberSupportersRequest request = new DeleteMemberSupportersRequest(Set.of(1L, 2L));
		Set<Long> memberIds = Set.of(10L, 20L);
		given(adminMemberActivityService.deleteMemberActivities(request.memberActivityIds(), MemberType.SUPPORTERS))
			.willReturn(memberIds);

		// when
		adminMemberSupportersUseCase.deleteMemberSupportersList(request);

		// then
		verify(adminMemberService).deleteMembersIfNoActivity(memberIds);
	}

	private MemberSupportersListProjection supportersProjection(Long memberActivityId, String name) {
		return new MemberSupportersListProjection(
			memberActivityId,
			name,
			"01012345678",
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ACTIVE,
			"memo"
		);
	}

	private MemberSupportersDetailProjection supportersDetailProjection(Long memberActivityId) {
		return new MemberSupportersDetailProjection(
			memberActivityId,
			"김서포터",
			"01012345678",
			"supporters@ject.kr",
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
}
