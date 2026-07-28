package org.ject.support.admin.member.dto.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.response.MemberSupportersListResponse;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberPageResultTest {

	@Test
	@DisplayName("조회 결과가 size보다 많으면 응답을 자르고 다음 커서를 반환한다")
	void 조회_결과가_size보다_많으면_응답을_자르고_다음_커서를_반환한다() {
		// given
		MemberPageResult<MemberSupportersListProjection> pageResult = MemberPageResult.of(
			List.of(
				supportersProjection(3L, "첫 번째"),
				supportersProjection(2L, "두 번째"),
				supportersProjection(1L, "세 번째")
			),
			3L
		);

		// when
		CursorPageResponse<MemberSupportersListResponse> response = pageResult.toCursorPageResponse(
			2,
			MemberSupportersListResponse::from,
			MemberSupportersListProjection::memberActivityId
		);

		// then
		assertThat(response.content())
			.extracting(MemberSupportersListResponse::name)
			.containsExactly("첫 번째", "두 번째");
		assertThat(response.size()).isEqualTo(2);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.nextCursor()).isEqualTo(2L);
		assertThat(response.totalCount()).isEqualTo(3L);
	}

	@Test
	@DisplayName("조회 결과가 size 이하면 전체 응답을 반환하고 다음 커서를 반환하지 않는다")
	void 조회_결과가_size_이하면_전체_응답을_반환하고_다음_커서를_반환하지_않는다() {
		// given
		MemberPageResult<MemberSupportersListProjection> pageResult = MemberPageResult.of(
			List.of(
				supportersProjection(2L, "첫 번째"),
				supportersProjection(1L, "두 번째")
			),
			2L
		);

		// when
		CursorPageResponse<MemberSupportersListResponse> response = pageResult.toCursorPageResponse(
			2,
			MemberSupportersListResponse::from,
			MemberSupportersListProjection::memberActivityId
		);

		// then
		assertThat(response.content())
			.extracting(MemberSupportersListResponse::name)
			.containsExactly("첫 번째", "두 번째");
		assertThat(response.size()).isEqualTo(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.totalCount()).isEqualTo(2L);
	}

	@Test
	@DisplayName("조회 결과가 없으면 빈 응답을 반환한다")
	void 조회_결과가_없으면_빈_응답을_반환한다() {
		// given
		MemberPageResult<MemberSupportersListProjection> pageResult = MemberPageResult.of(List.of(), 0L);

		// when
		CursorPageResponse<MemberSupportersListResponse> response = pageResult.toCursorPageResponse(
			30,
			MemberSupportersListResponse::from,
			MemberSupportersListProjection::memberActivityId
		);

		// then
		assertThat(response.content()).isEmpty();
		assertThat(response.size()).isEqualTo(30);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.totalCount()).isZero();
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
