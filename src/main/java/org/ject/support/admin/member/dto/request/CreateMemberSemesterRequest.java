package org.ject.support.admin.member.dto.request;

import java.util.List;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMemberSemesterRequest(

	/*
	필수 항목: 이름, 전화번호, 이메일, 직군(포지션), 활동 상태, 지원자 신분, 모집단위, 기수id
	선택 항목: 팀id, 비고, 관심도메인, 거주지역, 직무 관련 경험
	 */
	@NotBlank(message = "이름을 입력해주세요")
	@Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
	@Pattern(regexp = "^[^\\s]+$", message = "이름은 공백없이 입력해주세요")
	String name,

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 30, message = "이메일은 30자 이하로 입력해주세요.")
	String email,

	@NotBlank(message = "전화번호를 입력해주세요")
	@Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력해주세요.")
	String phoneNumber,

	@NotNull(message = "포지션을 선택해주세요")
	JobFamily jobFamily,

	@NotNull(message = "모집 단위를 선택해주세요")
	RecruitTypeDetail recruitTypeDetail,

	@NotNull(message = "활동 상태를 선택해주세요")
	ActivityStatus activityStatus,

	@NotNull(message = "지원자 신분을 선택해주세요")
	CareerDetails careerDetails,

	@NotNull(message = "기수를 선택해주세요")
	Long semesterId,

	Long teamId,

	ExperiencePeriod experiencePeriod,

	@Size(max = 100, message = "비고는 100자 이하로 입력해주세요.")
	String memo,

	@Size(min = 1, max = 3, message = "관심 도메인은 최소 1개부터 최대 3개까지 선택 가능합니다.")
	List<String> interestedDomains,

	Region region

) implements CreateMemberRequest {
}
