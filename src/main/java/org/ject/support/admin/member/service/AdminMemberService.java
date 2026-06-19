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

	// email 기준 구성원 조회 또는 신상정보 생성
	public Long findOrCreateMember(CreateMemberSemesterRequest request) {
		return memberRepository.findByEmail(request.email())
			.map(Member::getId)
			.orElseGet(() -> createMember(request));
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
