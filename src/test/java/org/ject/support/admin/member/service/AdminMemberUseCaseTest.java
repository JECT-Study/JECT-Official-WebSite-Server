package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
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

}
