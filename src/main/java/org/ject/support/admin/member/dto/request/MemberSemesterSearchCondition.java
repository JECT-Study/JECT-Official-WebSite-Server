package org.ject.support.admin.member.dto.request;

import java.util.List;

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
	// 직군 (다중 선택)
	List<JobFamily> jobFamilies,
	// 모집 단위 (다중 선택)
	List<RecruitTypeDetail> recruitTypeDetails,
	// 신분 (다중 선택)
	List<CareerDetails> careerDetails,
	// 팀 (다중 선택)
	List<@Positive Long> teamIds,
	// 활동 상태 (다중 선택)
	List<ActivityStatus> statuses
) {
	//size가 없으면 기본 30개
	public int getSizeOrDefault() {
		return size == null ? 30 : size;
	}
}
