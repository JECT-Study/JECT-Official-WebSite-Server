package org.ject.support.admin.member.dto.request;

import java.util.List;

import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMemberMakersRequest(

	// 필수 항목: 이름, 전화번호, 이메일, 직군(포자션), 지원자 신분, 소속, 모집단위

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

	@NotNull(message = "지원자 신분을 선택해주세요")
	CareerDetails careerDetails,

	@NotNull(message = "소속을 선택해주세요")
	MakersTeam makersTeam,

	@NotNull(message = "모집 단위를 선택해주세요")
	RecruitTypeDetail recruitTypeDetail,

	// 선택 항목: 거주지역, 관심도메인, 직무관련경험, 멘토링 가능 여부, 프로젝트 충원 가능 여부, 연사 가능 여부, 경력, 기술, 회사, 공유 가능한 전문 주제, 활동 증명서 번호, 비고

	Region region,

	@Size(min = 1, max = 3, message = "관심 도메인은 최소 1개부터 최대 3개까지 선택 가능합니다.")
	List<String> interestedDomains,

	ExperiencePeriod experiencePeriod,

	Availability mentoringAvailability,

	Availability projectSupplementAvailability,

	Availability speakerAvailability,

 	CareerLevel careerLevel,

	@Size(max = 255, message = "기술은 255자 이하로 입력해주세요.")
	String skills,

	@Size(max = 30, message = "회사는 30자 이하로 입력해주세요.")
	String company,

	@Size(max = 30, message = "공유 가능한 전문 주제는 30자 이하로 입력해주세요.")
	String expertTopics,

	@Size(max = 20, message = "활동 증명서 번호는 20자 이하로 입력해주세요.")
	String activityCertNumber,

	@Size(max = 100, message = "비고는 100자 이하로 입력해주세요.")
	String memo
) implements CreateMemberRequest {
}
