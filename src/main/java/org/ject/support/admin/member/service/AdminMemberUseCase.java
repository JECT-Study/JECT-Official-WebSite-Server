package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.response.SearchMemberSemesterResponse;
import org.ject.support.admin.member.dto.result.SearchMemberSemesterPageResult;
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


	// 일반 구성원 목록 조회 (커서 기반 페이징)
	@Transactional(readOnly=true)
	public CursorPageResponse<SearchMemberSemesterResponse> searchMemberSemester(
		MemberSemesterSearchCondition condition
	) {
		// 기수 유효성 체크
		validateSemester(condition.semesterId());
		// 팀 유효성 체크
		validateTeams(condition.semesterId(), condition.teamIds());
		// 활동 상태 체크
		validateStatuses(condition.statuses(), MemberType.SEMESTER);
		// size만큼 조회 + 전체 행 수 조회
		SearchMemberSemesterPageResult pageResult = adminMemberActivityService.searchMemberSemesterList(condition);
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
	private void validateTeam(Long semesterId, Long teamId) {
		if (teamId == null) {
			return;
		}

		if (!adminMemberTeamService.getTeamIdsBySemesterId(semesterId).contains(teamId)) {
			throw new MemberException(NOT_FOUND_TEAM_OF_SEMESTER);
		}
	}

	private void validateTeams(Long semesterId, List<Long> teamIds) {
		if(teamIds == null || teamIds.isEmpty()){
			return;
		}

		// 팀이 있는데 기수가 없으면 예외
		if (semesterId == null) {
			throw new MemberException(REQUIRED_SEMESTER_FOR_TEAM_FILTER);
		}

		// 조회 결과 List에서 바로 containsAll() -> O(N*M)
		// Set에 담고 O(1)로 비교시 -> O(N+M)
		Set<Long> validTeamIds = new HashSet<>(
			adminMemberTeamService.getTeamIdsBySemesterId(semesterId)
		);

		if(!validTeamIds.containsAll(teamIds)) {
			throw new MemberException(NOT_FOUND_TEAM_OF_SEMESTER);
		}
	}

	// 구성원 유형에서 허용되는 활동 상태인지 검증
	private void validateStatuses(List<ActivityStatus> statuses, MemberType type) {
		if (!ActivityStatus.isAllAvailableFor(statuses, type)) {
			throw new MemberException(INVALID_ACTIVITY_STATUS);
		}
	}

}
