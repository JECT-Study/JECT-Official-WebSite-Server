package org.ject.support.admin.member.dto.request;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record MemberSemesterSearchCondition(

	//-- 페이징 --

	//MemberActivityId 기준 내림차순
	@Positive
	Long cursor,

	// 정책 변경에 대비해 여유롭게 설정
	@Min(1)
	@Max(100)
	Integer size,

	//-- 필터 --

	// 기수
	@Positive
	Long semesterId,
	// 직군
	JobFamily jobFamily,
	// 모집 단위
	RecruitTypeDetail recruitTypeDetail,
	// 신분
	CareerDetails careerDetails,
	// 팀
	@Positive
	Long teamId,
	// 활동 상태
	ActivityStatus status
) {
	//size가 없으면 기본 30개
	public int getSizeOrDefault() {
		return size == null ? 30 : size;
	}
}
