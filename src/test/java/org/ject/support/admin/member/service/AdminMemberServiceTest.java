package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private AdminMemberService adminMemberService;

	private CreateMemberSemesterRequest createMemberSemesterRequest() {
		return new CreateMemberSemesterRequest(
			"김젝트",
			"jectkim@ject.kr",
			"01012345678",
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			1L,
			null,
			ExperiencePeriod.ONE_TO_TWO,
			"memo",
			List.of("HEALTHCARE", "FINTECH", "AI"),
			Region.SEOUL
		);
	}

	@Test
	@DisplayName("입력한 이메일로 기존 구성원이 존재하지 않으면 새로 저장한다")
	void 입력한_이메일로_기존_구성원이_존재하지_않으면_새로_저장한다() {
	    // given
	    CreateMemberSemesterRequest request = createMemberSemesterRequest();
		Member savedMember = mock(Member.class);
		given(savedMember.getId()).willReturn(1L);

		given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());
		given(memberRepository.save(any(Member.class))).willReturn(savedMember);

	    // when
	    Long memberId = adminMemberService.findOrCreateMember(request);

	    // then
		assertThat(memberId).isEqualTo(1L);
		verify(memberRepository).findByEmail(request.email());

		// 실제 save 시점에 전달된 인자 확인
		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(memberCaptor.capture());

		Member member = memberCaptor.getValue();
		assertThat(member.getEmail()).isEqualTo(request.email());
		assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(member.getName()).isEqualTo(request.name());
		assertThat(member.getRegion()).isEqualTo(request.region());
		assertThat(member.getInterestedDomains()).containsExactlyElementsOf(request.interestedDomains());
	}

	@Test
	@DisplayName("입력한 이메일로 기존 구성원이 존재하면 새로 저장하지 않는다")
	void 입력한_이메일로_기존_구성원이_존재하면_새로_저장하지_않는다() {
	    // given
	    CreateMemberSemesterRequest request = createMemberSemesterRequest();
		Member existMember = mock(Member.class);
		given(existMember.getId()).willReturn(3L);

		given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(existMember));

	    // when
	    Long memberId = adminMemberService.findOrCreateMember(request);

	    // then
		assertThat(memberId).isEqualTo(existMember.getId());
		verify(memberRepository).findByEmail(request.email());
		verify(memberRepository, never()).save(any(Member.class));
	}
}
