package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import static org.ject.support.domain.member.exception.MemberErrorCode.ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY;

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

	// 동일 기수 일반 구성원 활동 중복 검증
	private void validateDuplicateSemesterActivity(Long memberId, Long semesterId) {
		if (memberActivityRepository.existsSemesterActivity(memberId, MemberType.SEMESTER, semesterId)) {
			throw new MemberException(ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY);
		}
	}
}
