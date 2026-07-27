package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.*;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.result.MemberMakersListPageResult;
import org.ject.support.admin.member.dto.result.SearchMemberSemesterPageResult;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

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
			request.activityStatus(),
			request.careerDetails(),
			request.experiencePeriod(),
			request.memo(),
			request.semesterId(),
			request.teamId()
		);

		memberActivityRepository.save(memberActivity);
	}

	// 메이커스팀 구성원 추가
	public void createMemberMakersActivity(CreateMemberMakersRequest request, Long memberId) {
		// ACTIVE 상태로 추가할 때 기존 활동 중 이력 검증
		if (request.activityStatus() == ActivityStatus.ACTIVE) {
			validateDuplicateActiveMakersActivity(memberId);
		}

		// MemberActivity생성, 내부에서 MemberMakers 생성
		MemberActivity memberActivity = MemberActivity.createMakersActivity(
			memberId,
			request.jobFamily(),
			request.recruitTypeDetail(),
			request.activityStatus(),
			request.careerDetails(),
			request.experiencePeriod(),
			request.memo(),
			request.makersTeam(),
			request.mentoringAvailability(),
			request.projectSupplementAvailability(),
			request.speakerAvailability(),
			request.careerLevel(),
			request.skills(),
			request.company(),
			request.expertTopics(),
			request.activityCertNumber()
		);

		memberActivityRepository.save(memberActivity);
	}

	// 운영 서포터즈 구성원 추가
	public void createMemberSupportersActivity(CreateMemberSupportersRequest request, Long memberId) {
		// 운영 서포터즈 타입과 ACTIVE 중복 여부 검증
		validateSupportersMemberType(request.memberType());
		if (request.activityStatus() == ActivityStatus.ACTIVE) {
			validateDuplicateActiveSupportersActivity(memberId);
		}

		// MemberActivity생성, 내부에서 MemberSupporters 생성
		MemberActivity memberActivity = MemberActivity.createSupportersActivity(
			memberId,
			request.jobFamily(),
			request.recruitTypeDetail(),
			request.activityStatus(),
			request.startDate(),
			request.endDate(),
			request.activityCertNumber(),
			request.memo()
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

	// 메이커스팀 구성원 목록 조회
	public MemberMakersListPageResult getMemberMakersList(MemberMakersListRequest request) {
		List<MemberMakersListProjection> projections = memberActivityRepository.findMemberMakersList(request.cursor(), request.getSizeOrDefault()+1);
		long count = memberActivityRepository.countMemberMakersList();
		return new MemberMakersListPageResult(projections, count);
	}

	// 메이커스팀 구성원 상세 조회
	public MemberMakersDetailProjection getMemberMakersDetail(Long memberActivityId) {
		return memberActivityRepository.findMemberMakersDetail(memberActivityId)
			.orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
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

	// ACTIVE 상태인 메이커스팀 활동 존재 여부 검증(메이커스팀 활동은 반드시 1개만 활성 상태여야 함)
	private void validateDuplicateActiveMakersActivity(Long memberId) {
		if (memberActivityRepository.existsActiveMakersActivityByMemberId(memberId)) {
			throw new MemberException(ALREADY_EXIST_ACTIVE_MEMBER_MAKERS_ACTIVITY);
		}
	}

	// 운영 서포터즈 구성원 타입 검증
	private void validateSupportersMemberType(MemberType memberType) {
		if (memberType != MemberType.SUPPORTERS) {
			throw new MemberException(INVALID_MEMBER_TYPE);
		}
	}

	// ACTIVE 상태인 운영 서포터즈 활동 존재 여부 검증
	private void validateDuplicateActiveSupportersActivity(Long memberId) {
		if (memberActivityRepository.existsActiveSupportersActivityByMemberId(memberId)) {
			throw new MemberException(ALREADY_EXIST_ACTIVE_MEMBER_SUPPORTERS_ACTIVITY);
		}
	}
}
