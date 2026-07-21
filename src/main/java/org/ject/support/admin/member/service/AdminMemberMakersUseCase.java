package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberMakersUseCase {

	private final AdminMemberService adminMemberService;
	private final AdminMemberActivityService adminMemberActivityService;

	// 메이커스팀 구성원 추가
	@Transactional
	public void createMemberMakers(CreateMemberMakersRequest request) {
		// email 기준 기존 Member 조회 또는 신규 생성
		Long memberId = adminMemberService.findOrCreateMember(request);

		// memberId 기준 MemberActivity와 MemberMakers 생성 및 저장
		adminMemberActivityService.createMemberMakersActivity(request, memberId);
	}
}
