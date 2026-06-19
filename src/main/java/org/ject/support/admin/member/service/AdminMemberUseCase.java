package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberUseCase {

	private final AdminMemberService adminMemberService;
	private final AdminMemberActivityService adminMemberActivityService;
	private final SemesterInquiryUsecase semesterInquiryUsecase;

	//일반 구성원 생성
	@Transactional
	public void createMemberSemester(CreateMemberSemesterRequest request) {
		//기수 검증, 유효하지 않은 기수는 조회 시점에 semester쪽에서 예외 처리
		semesterInquiryUsecase.getSemester(request.semesterId());

		// email 기준 기존 Member 조회 또는 신규 생성
		Long memberId = adminMemberService.findOrCreateMember(request);

		//memberId 기준으로 MemberActivity + MemberSemester 생성 후 저장
		adminMemberActivityService.createMemberSemesterActivity(request, memberId);

	}

}
