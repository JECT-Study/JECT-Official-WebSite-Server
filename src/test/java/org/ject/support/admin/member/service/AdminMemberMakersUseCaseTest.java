package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.response.MemberMakersListResponse;
import org.ject.support.admin.member.dto.result.MemberMakersListPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
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
		MemberMakersListPageResult pageResult = new MemberMakersListPageResult(
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
		MemberMakersListPageResult pageResult = new MemberMakersListPageResult(
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
}
