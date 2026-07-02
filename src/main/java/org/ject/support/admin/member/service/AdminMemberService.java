package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

	private final MemberRepository memberRepository;

	/*
	(2026.06.23)
	1. email로 삭제 포함 구성원 신상 전부 조회
	2. 삭제된 구성원 존재시: isDeleted를 복구하고 새로 입력받은 값으로 덮어쓰기
	3. 활성 구성원 존재시: 기존 값 재사용 Todo: 대체 가능한 값은 새로운 입력으로 대체
	4. 미 존재시: 새로 생성
	 */

	// email 기준 구성원 조회 또는 신상정보 생성
	public Long findOrCreateMember(CreateMemberSemesterRequest request) {
		return memberRepository.findByEmailIncludingDeleted(request.email())
			.map(member -> useExistingMember(member, request))
			.orElseGet(() -> createMember(request));
	}

	// 기존 구성원 상태에 따라 재사용 또는 복구
	private Long useExistingMember(Member member, CreateMemberSemesterRequest request) {
		if (member.getIsDeleted()) {
			return restoreMember(member, request);
		}

		return member.getId();
	}

	// 삭제된 구성원 신상정보 갱신 후 복구
	private Long restoreMember(Member member, CreateMemberSemesterRequest request) {
		member.restore(
			request.name(),
			request.phoneNumber(),
			request.interestedDomains(),
			request.region()
		);
		return member.getId();
	}

	// 구성원 신상정보 생성
	private Long createMember(CreateMemberSemesterRequest request) {
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

}
