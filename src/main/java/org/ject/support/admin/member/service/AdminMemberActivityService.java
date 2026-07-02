package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.result.SearchMemberSemesterPageResult;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import static org.ject.support.domain.member.exception.MemberErrorCode.ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberActivityService {

	private final MemberActivityRepository memberActivityRepository;


	public void createMemberSemesterActivity(CreateMemberSemesterRequest request, Long memberId) {
		// 추가하려는 대상이 같은 기수에 이미 등록되어있는지 중복 검증
		validateDuplicateSemesterActivity(memberId, request.semesterId());

		// MemberActivity생성, 내부에서 MemberSemester 생성
		MemberActivity memberActivity = MemberActivity.createSemesterActivity(
			memberId,
			request.jobFamily(),
			request.recruitTypeDetail(),
			request.careerDetails(),
			request.experiencePeriod(),
			request.memo(),
			request.semesterId(),
			request.teamId()
		);

		memberActivityRepository.save(memberActivity);
	}

	// 동적 필터로 일반 구성원 목록 조회
	public SearchMemberSemesterPageResult searchMemberSemesterList(MemberSemesterSearchCondition condition) {
		// size+1로 조회 (다음 페이지 유무 확인)
		List<SearchMemberSemesterProjection> projections =
			memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
		long totalCount = memberActivityRepository.countMemberSemesters(condition);
		return new SearchMemberSemesterPageResult(projections, totalCount);
	}


	/*
	유틸, 검증 함수
	 */

	// 동일 기수 일반 구성원 활동 중복 검증
	private void validateDuplicateSemesterActivity(Long memberId, Long semesterId) {
		if (memberActivityRepository.existsSemesterActivity(memberId, MemberType.SEMESTER, semesterId)) {
			throw new MemberException(ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY);
		}
	}
}
