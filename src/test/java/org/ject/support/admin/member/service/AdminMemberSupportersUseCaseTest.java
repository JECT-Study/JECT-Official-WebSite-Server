package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;

import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.response.MemberSupportersListResponse;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
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
}
