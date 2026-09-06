package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.admin.member.dto.command.EditMemberCommand;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MemberActivityRepository memberActivityRepository;

	@InjectMocks
	private AdminMemberService adminMemberService;

	@Test
	@DisplayName("존재하지 않는 구성원은 수정할 수 없다")
	void 존재하지_않는_구성원은_수정할_수_없다() {
		// given
		Long memberId = 1L;
		EditMemberCommand command = new EditMemberCommand("수정된이름", null, null, null, null);
		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminMemberService.editMember(memberId, command))
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
	}

	@Test
	@DisplayName("다른 구성원이 사용 중인 이메일로 변경할 수 없다")
	void 다른_구성원이_사용_중인_이메일로_변경할_수_없다() {
		// given
		Member member = member().email("current@ject.kr").build();
		Member otherMember = member().email("other@ject.kr").build();
		ReflectionTestUtils.setField(member, "id", 1L);
		ReflectionTestUtils.setField(otherMember, "id", 2L);
		EditMemberCommand command = new EditMemberCommand(null, "other@ject.kr", null, null, null);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.findByEmailIncludingDeleted("other@ject.kr")).willReturn(Optional.of(otherMember));

		// when & then
		assertThatThrownBy(() -> adminMemberService.editMember(1L, command))
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.DUPLICATE_EMAIL);
		assertThat(member.getEmail()).isEqualTo("current@ject.kr");
	}

	@Test
	@DisplayName("삭제된 구성원이 사용했던 이메일로 변경할 수 없다")
	void 삭제된_구성원이_사용했던_이메일로_변경할_수_없다() {
		// given
		Member member = member().email("current@ject.kr").build();
		Member deletedMember = member().email("deleted@ject.kr").deleted().build();
		ReflectionTestUtils.setField(member, "id", 1L);
		ReflectionTestUtils.setField(deletedMember, "id", 2L);
		EditMemberCommand command = new EditMemberCommand(null, "deleted@ject.kr", null, null, null);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.findByEmailIncludingDeleted("deleted@ject.kr")).willReturn(Optional.of(deletedMember));

		// when & then
		assertThatThrownBy(() -> adminMemberService.editMember(1L, command))
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.DUPLICATE_EMAIL);
	}

	@Test
	@DisplayName("현재 이메일과 동일한 이메일로 수정할 수 있다")
	void 현재_이메일과_동일한_이메일로_수정할_수_있다() {
		// given
		Member member = member().email("current@ject.kr").build();
		EditMemberCommand command = new EditMemberCommand("수정된이름", "current@ject.kr", null, null, null);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		// when
		adminMemberService.editMember(1L, command);

		// then
		assertThat(member.getName()).isEqualTo("수정된이름");
		assertThat(member.getEmail()).isEqualTo("current@ject.kr");
		verify(memberRepository, never()).findByEmailIncludingDeleted(any());
	}

	private CreateMemberSemesterRequest createMemberSemesterRequest() {
		return new CreateMemberSemesterRequest(
			"김젝트",
			"jectkim@ject.kr",
			"01012345678",
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ACTIVE,
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

		given(memberRepository.findByEmailIncludingDeleted(request.email())).willReturn(Optional.empty());
		given(memberRepository.save(any(Member.class))).willReturn(savedMember);

	    // when
	    Long memberId = adminMemberService.findOrCreateMember(request);

	    // then
		assertThat(memberId).isEqualTo(1L);
		verify(memberRepository).findByEmailIncludingDeleted(request.email());

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
	@DisplayName("입력한 이메일로 기존 구성원이 존재하면 값을 덮어쓰고 새로 저장하지 않는다")
	void 입력한_이메일로_기존_구성원이_존재하면_값을_덮어쓰고_새로_저장하지_않는다() {
	    // given
	    CreateMemberSemesterRequest request = createMemberSemesterRequest();
		Member existMember = Member.create(
			"기존 구성원",
			request.email(),
			"01087654321",
			List.of("COMMERCE"),
			Region.BUSAN
		);
		ReflectionTestUtils.setField(existMember, "id", 3L);

		given(memberRepository.findByEmailIncludingDeleted(request.email())).willReturn(Optional.of(existMember));

	    // when
	    Long memberId = adminMemberService.findOrCreateMember(request);

	    // then
		assertThat(memberId).isEqualTo(existMember.getId());
		assertThat(existMember.getName()).isEqualTo(request.name());
		assertThat(existMember.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(existMember.getInterestedDomains()).containsExactlyElementsOf(request.interestedDomains());
		assertThat(existMember.getRegion()).isEqualTo(request.region());
		verify(memberRepository).findByEmailIncludingDeleted(request.email());
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	@DisplayName("입력한 이메일로 삭제된 구성원이 존재하면 복구하고 값을 덮어쓴다")
	void 입력한_이메일로_삭제된_구성원이_존재하면_복구하고_값을_덮어쓴다() {
	    // given
	    CreateMemberSemesterRequest request = createMemberSemesterRequest();
		Member deletedMember = Member.create(
			"삭제된 구성원",
			request.email(),
			"01087654321",
			List.of("COMMERCE"),
			Region.BUSAN
		);

		//Todo: 삭제 API 구현 시 도메인 로직을 사용하도록 수정
		ReflectionTestUtils.setField(deletedMember, "id", 7L);
		ReflectionTestUtils.setField(deletedMember, "isDeleted", true);

		given(memberRepository.findByEmailIncludingDeleted(request.email())).willReturn(Optional.of(deletedMember));

	    // when
	    Long memberId = adminMemberService.findOrCreateMember(request);

	    // then
		assertThat(memberId).isEqualTo(7L);
		assertThat(deletedMember.getIsDeleted()).isFalse();
		assertThat(deletedMember.getName()).isEqualTo(request.name());
		assertThat(deletedMember.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(deletedMember.getInterestedDomains()).containsExactlyElementsOf(request.interestedDomains());
		assertThat(deletedMember.getRegion()).isEqualTo(request.region());
		verify(memberRepository).findByEmailIncludingDeleted(request.email());
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	@DisplayName("남은 활동이 없으면 구성원을 삭제한다")
	void 남은_활동이_없으면_구성원을_삭제한다() {
		// given
		Long memberId = 1L;
		Member member = mock(Member.class);
		given(memberActivityRepository.existsByMemberId(memberId)).willReturn(false);
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

		// when
		adminMemberService.deleteMemberIfNoActivity(memberId);

		// then
		verify(memberRepository).delete(member);
	}

	@Test
	@DisplayName("남은 활동은 없지만 구성원을 찾을 수 없으면 삭제에 실패한다")
	void 남은_활동은_없지만_구성원을_찾을_수_없으면_삭제에_실패한다() {
		// given
		Long memberId = 1L;
		given(memberActivityRepository.existsByMemberId(memberId)).willReturn(false);
		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminMemberService.deleteMemberIfNoActivity(memberId))
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
		verify(memberRepository, never()).delete(any(Member.class));
	}

	@Test
	@DisplayName("다른 활동이 남아 있으면 구성원을 유지한다")
	void 다른_활동이_남아_있으면_구성원을_유지한다() {
		// given
		Long memberId = 1L;
		given(memberActivityRepository.existsByMemberId(memberId)).willReturn(true);

		// when
		adminMemberService.deleteMemberIfNoActivity(memberId);

		// then
		verify(memberRepository, never()).findById(memberId);
		verify(memberRepository, never()).delete(any(Member.class));
	}

	@Test
	@DisplayName("여러 구성원 중 남은 활동이 없는 구성원만 삭제한다")
	void 여러_구성원_중_남은_활동이_없는_구성원만_삭제한다() {
		// given
		Set<Long> memberIds = Set.of(1L, 2L, 3L);
		Member first = mock(Member.class);
		Member third = mock(Member.class);
		given(memberActivityRepository.findMemberIdsWithActivity(memberIds)).willReturn(Set.of(2L));
		given(memberRepository.findAllById(any())).willReturn(List.of(first, third));

		// when
		adminMemberService.deleteMembersIfNoActivity(memberIds);

		// then
		verify(memberRepository).findAllById(argThat(ids -> {
			assertThat(ids).containsExactlyInAnyOrder(1L, 3L);
			return true;
		}));
		verify(memberRepository).deleteAll(List.of(first, third));
	}

	@Test
	@DisplayName("일괄 삭제할 구성원 일부를 찾을 수 없으면 모두 삭제하지 않는다")
	void 일괄_삭제할_구성원_일부를_찾을_수_없으면_모두_삭제하지_않는다() {
		// given
		Set<Long> memberIds = Set.of(1L, 2L);
		Member member = mock(Member.class);
		given(memberActivityRepository.findMemberIdsWithActivity(memberIds)).willReturn(Set.of());
		given(memberRepository.findAllById(any())).willReturn(List.of(member));

		// when & then
		assertThatThrownBy(() -> adminMemberService.deleteMembersIfNoActivity(memberIds))
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
		verify(memberRepository, never()).deleteAll(any());
	}

	@Test
	@DisplayName("모든 구성원에게 활동이 남아 있으면 모두 유지한다")
	void 모든_구성원에게_활동이_남아_있으면_모두_유지한다() {
		// given
		Set<Long> memberIds = Set.of(1L, 2L);
		given(memberActivityRepository.findMemberIdsWithActivity(memberIds)).willReturn(memberIds);

		// when
		adminMemberService.deleteMembersIfNoActivity(memberIds);

		// then
		verify(memberRepository, never()).findAllById(any());
		verify(memberRepository, never()).deleteAll(any());
	}
}
