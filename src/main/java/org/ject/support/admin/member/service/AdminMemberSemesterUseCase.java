package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.*;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.response.SearchMemberSemesterResponse;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberSemesterUseCase {

	private final AdminMemberService adminMemberService;
	private final AdminMemberActivityService adminMemberActivityService;
	private final AdminMemberTeamService adminMemberTeamService;
	private final SemesterInquiryUsecase semesterInquiryUsecase;

	// 일반 구성원 생성 흐름 처리
	@Transactional
	public void createMemberSemester(CreateMemberSemesterRequest request) {
		// 유효성 검증
		semesterInquiryUsecase.getSemester(request.semesterId());
		validateCreateTeam(request.semesterId(), request.teamId());

		// email 기준 기존 Member 조회 또는 신규 생성
		Long memberId = adminMemberService.findOrCreateMember(request);

		// memberId 기준 MemberActivity와 MemberSemester 생성 및 저장
		adminMemberActivityService.createMemberSemesterActivity(request, memberId);
	}


	// 일반 구성원 목록 조회 (커서 기반 페이징)
	@Transactional(readOnly=true)
	public CursorPageResponse<SearchMemberSemesterResponse> searchMemberSemester(
		MemberSemesterSearchCondition condition
	) {
		// 유효성 검증
		validateSemester(condition.semesterId());
		validateSearchTeam(condition.semesterId(), condition.teamId());
		validateStatus(condition.status(), MemberType.SEMESTER);
		// size만큼 조회 + 전체 행 수 조회
		MemberPageResult<SearchMemberSemesterProjection> pageResult =
			adminMemberActivityService.searchMemberSemesterList(condition);
		// 페이징 값 처리
		boolean hasNext = pageResult.content().size() > condition.getSizeOrDefault();
		long totalCount = pageResult.totalCount();
		List<SearchMemberSemesterProjection> content = hasNext
			? pageResult.content().subList(0,condition.getSizeOrDefault())
			: pageResult.content();

		List<SearchMemberSemesterResponse> responses = content.stream()
			.map(SearchMemberSemesterResponse::from)
			.toList();

		Long nextCursor = hasNext && !content.isEmpty()
			? content.get(content.size()-1).memberActivityId()
			: null;

		// 응답
		return CursorPageResponse.of(
			responses,
			condition.getSizeOrDefault(),
			hasNext,
			nextCursor,
			totalCount
		);
	}

	/*
	유틸 함수
	 */

	// 존재하는 기수인지 검증
	private void validateSemester(Long semesterId) {
		if(semesterId == null){
			return;
		}
		// 존재하지 않으면 semester 도메인에서 예외 처리
		semesterInquiryUsecase.getSemester(semesterId);
	}

	// 선택한 팀의 기수 소속 검증
	private void validateCreateTeam(Long semesterId, Long teamId) {
		if (teamId == null) {
			return;
		}

		if (!adminMemberTeamService.getTeamIdsBySemesterId(semesterId).contains(teamId)) {
			throw new MemberException(NOT_FOUND_TEAM_OF_SEMESTER);
		}
	}

	// 조회 필터 팀의 기수 소속 및 유효성 검증
	private void validateSearchTeam(Long semesterId, Long teamId) {
		if(teamId == null){
			return;
		}

		// 팀이 있는데 기수가 없으면 잘못된 요청
		if (semesterId == null) {
			throw new MemberException(REQUIRED_SEMESTER_FOR_TEAM_FILTER);
		}

		List<Long> validTeamIds = adminMemberTeamService.getTeamIdsBySemesterId(semesterId);
		if(!validTeamIds.contains(teamId)) {
			throw new MemberException(NOT_FOUND_TEAM_OF_SEMESTER);
		}
	}

	// 구성원 유형에서 허용되는 활동 상태인지 검증
	private void validateStatus(ActivityStatus status, MemberType type) {
		if (status != null && !status.isAvailableFor(type)) {
			throw new MemberException(INVALID_ACTIVITY_STATUS);
		}
	}

}
