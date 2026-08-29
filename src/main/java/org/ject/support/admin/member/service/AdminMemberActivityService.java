package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.command.EditMemberActivityCommand;
import org.ject.support.domain.member.command.EditMemberMakersCommand;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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
		if (ActivityStatus.isActive(request.activityStatus())) {
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
		if (ActivityStatus.isActive(request.activityStatus())) {
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
	public MemberPageResult<SearchMemberSemesterProjection> searchMemberSemesterList(
		MemberSemesterSearchCondition condition
	) {
		// size+1로 조회 (다음 페이지 유무 확인)
		List<SearchMemberSemesterProjection> projections =
			memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
		long totalCount = memberActivityRepository.countMemberSemesters(condition);
		return MemberPageResult.of(projections, totalCount);
	}

	// 메이커스팀 구성원 목록 조회
	public MemberPageResult<MemberMakersListProjection> getMemberMakersList(MemberMakersListRequest request) {
		List<MemberMakersListProjection> projections = memberActivityRepository.findMemberMakersList(request.cursor(), request.getSizeOrDefault()+1);
		long count = memberActivityRepository.countMemberMakersList();
		return MemberPageResult.of(projections, count);
	}

	// 운영 서포터즈 구성원 목록 조회
	public MemberPageResult<MemberSupportersListProjection> getMemberSupportersList(MemberSupportersListRequest request) {
		List<MemberSupportersListProjection> projections = memberActivityRepository.findMemberSupportersList(
			request.cursor(),
			request.getSizeOrDefault() + 1
		);
		long count = memberActivityRepository.countMemberSupportersList();
		return MemberPageResult.of(projections, count);
	}

	public MemberActivity getMemberMakersActivity(Long memberActivityId) {
		return memberActivityRepository.findByIdAndMemberType(memberActivityId, MemberType.MAKERS)
			.orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER_MAKERS_ACTIVITY));
	}

	// 메이커스팀 구성원 상세 조회
	public MemberMakersDetailProjection getMemberMakersDetail(Long memberActivityId) {
		return memberActivityRepository.findMemberMakersDetail(memberActivityId)
			.orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
	}

	// 메이커스팀 구성원 활동정보 편집
	public Long editMemberMakersActivity(Long memberActivityId, EditMemberActivityCommand activityCommand,
		EditMemberMakersCommand makersCommand, ActivityStatus activityStatus) {
		MemberActivity memberActivity = getMemberMakersActivity(memberActivityId);
		memberActivity.editMakersActivity(activityCommand, makersCommand);
		if (activityStatus != null) {
			editMakersActivityStatus(memberActivity, activityStatus);
		}
		return memberActivity.getMemberId();
	}

	public void editMakersActivityStatus(MemberActivity memberActivity, ActivityStatus activityStatus) {
		if (memberActivity.isSameActivityStatus(activityStatus)) {
			return;
		}

		switch (activityStatus) {
			case ACTIVE -> {
				validateDuplicateActiveMakersActivity(memberActivity.getMemberId());
				memberActivity.activate();
			}
			case ENDED -> memberActivity.end();
			case DROPOUT -> memberActivity.dropOut();
			default -> throw new MemberException(INVALID_ACTIVITY_STATUS);
		}
	}

	// 구성원 유형에 맞는 활동 단건 삭제
	public Long deleteMemberActivity(Long memberActivityId, MemberType memberType) {
		MemberActivity memberActivity = memberActivityRepository.findByIdAndMemberType(memberActivityId, memberType)
			.orElseThrow(() -> new MemberException(getNotFoundActivityErrorCode(memberType)));
		memberActivityRepository.delete(memberActivity);
		return memberActivity.getMemberId();
	}

	// 구성원 유형에 맞는 활동 전체 검증 후 일괄 삭제
	public Set<Long> deleteMemberActivities(Set<Long> memberActivityIds, MemberType memberType) {
		List<MemberActivity> memberActivities = memberActivityRepository.findAllByIdInAndMemberType(
			memberActivityIds,
			memberType
		);
		Set<Long> foundIds = memberActivities.stream().map(MemberActivity::getId).collect(Collectors.toSet());
		memberActivityIds.stream().filter(id -> !foundIds.contains(id)).findFirst().ifPresent(invalidId -> {
			log.warn("구성원 활동 일괄 삭제 검증 실패: memberType={}, invalidMemberActivityId={}", memberType, invalidId);
			throw new MemberException(getNotFoundActivityErrorCode(memberType));
		});
		Set<Long> memberIds = memberActivities.stream()
			.map(MemberActivity::getMemberId)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		memberActivityRepository.deleteAll(memberActivities);
		return memberIds;
	}

	private MemberErrorCode getNotFoundActivityErrorCode(MemberType memberType) {
		return switch (memberType) {
			case SEMESTER -> NOT_FOUND_MEMBER_SEMESTER_ACTIVITY;
			case MAKERS -> NOT_FOUND_MEMBER_MAKERS_ACTIVITY;
			case SUPPORTERS -> NOT_FOUND_MEMBER_SUPPORTERS_ACTIVITY;
		};
	}

	// 운영 서포터즈 구성원 상세 조회
	public MemberSupportersDetailProjection getMemberSupportersDetail(Long memberActivityId) {
		return memberActivityRepository.findMemberSupportersDetail(memberActivityId)
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

	// ACTIVE 상태인 운영 서포터즈 활동 존재 여부 검증
	private void validateDuplicateActiveSupportersActivity(Long memberId) {
		if (memberActivityRepository.existsActiveSupportersActivityByMemberId(memberId)) {
			throw new MemberException(ALREADY_EXIST_ACTIVE_MEMBER_SUPPORTERS_ACTIVITY);
		}
	}
}
