package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.response.SearchMemberSemesterResponse;
import org.ject.support.admin.member.dto.result.SearchMemberSemesterPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberUseCaseTest {

	@Mock
	private AdminMemberService adminMemberService;

	@Mock
	private AdminMemberActivityService adminMemberActivityService;

	@Mock
	private AdminMemberTeamService adminMemberTeamService;

	@Mock
	private SemesterInquiryUsecase semesterInquiryUsecase;

	@InjectMocks
	private AdminMemberUseCase adminMemberUseCase;

	private CreateMemberSemesterRequest createMemberSemesterRequest() {
		return createMemberSemesterRequest(null);
	}

	private CreateMemberSemesterRequest createMemberSemesterRequest(Long teamId) {
		return new CreateMemberSemesterRequest(
			"김젝트",
			"jectkim@ject.kr",
			"01012345678",
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			1L,
			teamId,
			ExperiencePeriod.ONE_TO_TWO,
			"memo",
			List.of("HEALTHCARE", "FINTECH", "AI"),
			Region.SEOUL
		);
	}

	private MemberSemesterSearchCondition searchCondition(Integer size, Long semesterId, List<Long> teamIds) {
		return new MemberSemesterSearchCondition(
			null,
			size,
			semesterId,
			null,
			null,
			null,
			teamIds
		);
	}

	private SearchMemberSemesterProjection searchProjection(Long memberActivityId) {
		return new SearchMemberSemesterProjection(
			memberActivityId,
			"김젝트",
			JobFamily.BE,
			"01012345678",
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO
		);
	}

	/**
	 * 일반 구성원 추가 테스트
	 */
	@Test
	@DisplayName("일반 구성원을 생성한다")
	void 일반_구성원을_생성한다() {
	    // given
		CreateMemberSemesterRequest request = createMemberSemesterRequest();
		Long memberId = 1L;

		given(adminMemberService.findOrCreateMember(request)).willReturn(memberId);

	    // when
		adminMemberUseCase.createMemberSemester(request);

	    // then
		verify(semesterInquiryUsecase).getSemester(request.semesterId());
		verifyNoInteractions(adminMemberTeamService);
		verify(adminMemberService).findOrCreateMember(request);
		verify(adminMemberActivityService).createMemberSemesterActivity(request, memberId);
	}

	@Test
	@DisplayName("기수와 팀 검증을 통과하면 일반 구성원을 생성한다")
	void 기수와_팀_검증을_통과하면_일반_구성원을_생성한다() {
		// given
		CreateMemberSemesterRequest request = createMemberSemesterRequest(2L);
		Long memberId = 1L;
		given(adminMemberTeamService.getTeamIdsBySemesterId(request.semesterId()))
			.willReturn(List.of(request.teamId()));
		given(adminMemberService.findOrCreateMember(request)).willReturn(memberId);

		// when
		adminMemberUseCase.createMemberSemester(request);

		// then
		verify(semesterInquiryUsecase).getSemester(request.semesterId());
		verify(adminMemberTeamService).getTeamIdsBySemesterId(request.semesterId());
		verify(adminMemberService).findOrCreateMember(request);
		verify(adminMemberActivityService).createMemberSemesterActivity(request, memberId);
	}

	@Test
	@DisplayName("해당 기수에 속하지 않은 팀을 선택하면 예외가 발생한다")
	void 해당_기수에_속하지_않은_팀을_선택하면_예외가_발생한다() {
		// given
		CreateMemberSemesterRequest request = createMemberSemesterRequest(4L);
		given(adminMemberTeamService.getTeamIdsBySemesterId(request.semesterId()))
			.willReturn(List.of(1L, 2L, 3L));

		// when
		Throwable throwable = catchThrowable(() -> adminMemberUseCase.createMemberSemester(request));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_TEAM_OF_SEMESTER);
		verifyNoInteractions(adminMemberService);
		verifyNoInteractions(adminMemberActivityService);
	}

	@Test
	@DisplayName("존재하지 않는 기수로 구성원 생성을 시도하면 예외가 발생한다")
	void 존재하지_않는_기수로_구성원_생성을_시도하면_예외가_발생한다() {
	    // given
	    CreateMemberSemesterRequest request = createMemberSemesterRequest();
		SemesterException exception = new SemesterException(SemesterErrorCode.NOT_FOUND_SEMESTER);
	    given(semesterInquiryUsecase.getSemester(request.semesterId())).willThrow(exception);

	    // when
		Throwable throwable = catchThrowable(() -> adminMemberUseCase.createMemberSemester(request));

		// then
		assertThat(throwable).isSameAs(exception);
		verifyNoInteractions(adminMemberTeamService);
		verifyNoInteractions(adminMemberService);
		verifyNoInteractions(adminMemberActivityService);

	}

	/**
	 * 일반 구성원 목록 조회 테스트
	 */
	@Test
	@DisplayName("조회 결과가 size보다 많으면 다음 커서를 반환한다")
	void 조회_결과가_size보다_많으면_다음_커서를_반환한다() {
		// given
		MemberSemesterSearchCondition condition = searchCondition(2, null, null);
		SearchMemberSemesterPageResult pageResult = new SearchMemberSemesterPageResult(
			List.of(searchProjection(5L), searchProjection(4L), searchProjection(3L)),
			3L
		);
		given(adminMemberActivityService.searchMemberSemesterList(condition)).willReturn(pageResult);

		// when
		CursorPageResponse<SearchMemberSemesterResponse> response =
			adminMemberUseCase.searchMemberSemester(condition);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.nextCursor()).isEqualTo(4L);
		assertThat(response.totalCount()).isEqualTo(3L);
		verifyNoInteractions(semesterInquiryUsecase);
		verifyNoInteractions(adminMemberTeamService);
	}

	@Test
	@DisplayName("조회 결과가 size보다 적으면 다음 커서를 반환하지 않는다")
	void 조회_결과가_size보다_적으면_다음_커서를_반환하지_않는다() {
		// given
		MemberSemesterSearchCondition condition = searchCondition(3, null, null);
		SearchMemberSemesterPageResult pageResult = new SearchMemberSemesterPageResult(
			List.of(searchProjection(5L), searchProjection(4L)),
			2L
		);
		given(adminMemberActivityService.searchMemberSemesterList(condition)).willReturn(pageResult);

		// when
		CursorPageResponse<SearchMemberSemesterResponse> response =
			adminMemberUseCase.searchMemberSemester(condition);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.totalCount()).isEqualTo(2L);
	}

	@Test
	@DisplayName("조회 결과가 없으면 빈 응답을 반환한다")
	void 조회_결과가_없으면_빈_응답을_반환한다() {
		// given
		MemberSemesterSearchCondition condition = searchCondition(3, null, null);
		SearchMemberSemesterPageResult pageResult = new SearchMemberSemesterPageResult(List.of(), 0L);
		given(adminMemberActivityService.searchMemberSemesterList(condition)).willReturn(pageResult);

		// when
		CursorPageResponse<SearchMemberSemesterResponse> response =
			adminMemberUseCase.searchMemberSemester(condition);

		// then
		assertThat(response.content()).isEmpty();
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.totalCount()).isZero();
	}

	@Test
	@DisplayName("팀 필터만 있으면 예외가 발생한다")
	void 팀_필터만_있으면_예외가_발생한다() {
		// given
		MemberSemesterSearchCondition condition = searchCondition(3, null, List.of(1L));

		// when
		Throwable throwable = catchThrowable(() -> adminMemberUseCase.searchMemberSemester(condition));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.REQUIRED_SEMESTER_FOR_TEAM_FILTER);
		verifyNoInteractions(adminMemberActivityService);
	}

	@Test
	@DisplayName("해당 기수에 없는 팀으로 조회하면 예외가 발생한다")
	void 해당_기수에_없는_팀으로_조회하면_예외가_발생한다() {
		// given
		MemberSemesterSearchCondition condition = searchCondition(3, 1L, List.of(9L));
		given(adminMemberTeamService.getTeamIdsBySemesterId(condition.semesterId()))
			.willReturn(List.of(1L, 2L, 3L));

		// when
		Throwable throwable = catchThrowable(() -> adminMemberUseCase.searchMemberSemester(condition));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_TEAM_OF_SEMESTER);
		verify(semesterInquiryUsecase).getSemester(condition.semesterId());
		verifyNoInteractions(adminMemberActivityService);
	}
}
