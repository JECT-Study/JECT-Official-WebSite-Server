package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.NOT_FOUND_MEMBER;

import java.util.List;
import java.util.Set;

import org.ject.support.admin.member.dto.request.CreateMemberRequest;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

	private final MemberRepository memberRepository;
	private final MemberActivityRepository memberActivityRepository;

	/*
	(2026.06.23)
	1. email로 삭제 포함 구성원 신상 전부 조회
	2. 삭제된 구성원 존재시: isDeleted 복구
	3. 활성 구성원 존재시: 새로 입력받은 값으로 덮어쓰기
	4. 미 존재시: 새로 생성
	*/

	// email 기준 구성원 조회 또는 신상정보 생성
	public Long findOrCreateMember(CreateMemberRequest request) {
		return memberRepository.findByEmailIncludingDeleted(request.email())
			.map(member -> useExistingMember(member, request))
			.orElseGet(() -> createMember(request));
	}

	// 기존 구성원 복구 후 신상정보 갱신
	private Long useExistingMember(Member member, CreateMemberRequest request) {
		if (member.getIsDeleted()) {
			member.restore();
		}

		member.updateProfile(
			request.name(),
			request.phoneNumber(),
			request.interestedDomains(),
			request.region()
		);
		return member.getId();
	}

	// 구성원 신상정보 생성
	private Long createMember(CreateMemberRequest request) {
		Member member = Member.create(
			request.name(),
			request.email(),
			request.phoneNumber(),
			request.interestedDomains(),
			request.region()
		);

		Member savedMember = memberRepository.save(member);
		return savedMember.getId();
	}

	// 남은 구성원 활동이 없으면 구성원 삭제
	public void deleteMemberIfNoActivity(Long memberId) {
		if (memberActivityRepository.existsByMemberId(memberId)) {
			return;
		}
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
		memberRepository.delete(member);
	}

	// 남은 구성원 활동이 없는 구성원 일괄 삭제
	public void deleteMembersIfNoActivity(Set<Long> memberIds) {
		Set<Long> activeMemberIds = memberActivityRepository.findMemberIdsWithActivity(memberIds);
		List<Long> deleteMemberIds = memberIds.stream().filter(id -> !activeMemberIds.contains(id)).toList();
		if (deleteMemberIds.isEmpty()) {
			return;
		}
		List<Member> members = memberRepository.findAllById(deleteMemberIds);
		if (deleteMemberIds.size() != members.size()) {
			throw new MemberException(NOT_FOUND_MEMBER);
		}
		memberRepository.deleteAll(members);
	}

}
