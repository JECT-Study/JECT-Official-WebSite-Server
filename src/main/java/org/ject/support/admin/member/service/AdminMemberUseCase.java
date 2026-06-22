package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.NOT_FOUND_TEAM_OF_SEMESTER;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberUseCase {

	private final AdminMemberService adminMemberService;
	private final AdminMemberActivityService adminMemberActivityService;
	private final AdminMemberTeamService adminMemberTeamService;
	private final SemesterInquiryUsecase semesterInquiryUsecase;

	// 일반 구성원 생성 흐름 처리
	@Transactional
	public void createMemberSemester(CreateMemberSemesterRequest request) {
		// 기수 유효성 검증
		semesterInquiryUsecase.getSemester(request.semesterId());
		validateTeam(request.semesterId(), request.teamId());

		// email 기준 기존 Member 조회 또는 신규 생성
		Long memberId = adminMemberService.findOrCreateMember(request);

		// memberId 기준 MemberActivity와 MemberSemester 생성 및 저장
		adminMemberActivityService.createMemberSemesterActivity(request, memberId);
	}

	// 선택한 팀의 기수 소속 검증
	private void validateTeam(Long semesterId, Long teamId) {
		if (teamId == null) {
			return;
		}

		if (!adminMemberTeamService.getTeamIdsBySemesterId(semesterId).contains(teamId)) {
			throw new MemberException(NOT_FOUND_TEAM_OF_SEMESTER);
		}
	}
}
