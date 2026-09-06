package org.ject.support.admin.member.dto.request;

import java.util.List;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "메이커스팀 구성원 수정 요청. 전달하지 않은 항목은 기존 값 유지")
public record UpdateMemberMakersRequest(

	@Schema(description = "이름", example = "김젝트", maxLength = 20, nullable = true)
	@Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
	@Pattern(regexp = "^[^\\s]+$", message = "이름은 공백없이 입력해주세요")
	String name,

	@Schema(description = "이메일", example = "member@ject.kr", maxLength = 30, nullable = true)
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 30, message = "이메일은 30자 이하로 입력해주세요.")
	String email,

	@Schema(description = "전화번호", example = "01012345678", nullable = true)
	@Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력해주세요.")
	String phoneNumber,

	@Schema(description = "직군", example = "BE", allowableValues = {"PM", "PD", "FE", "BE"}, nullable = true)
	JobFamily jobFamily,

	@Schema(description = "지원자 신분", example = "EMPLOYEE", allowableValues = {"STUDENT", "EXPECTED_GRADUATE", "JOB_SEEKER", "BETWEEN_JOBS", "EMPLOYEE"}, nullable = true)
	CareerDetails careerDetails,

	@Schema(description = "메이커스팀 소속", example = "TEAM_1", allowableValues = {"TEAM_1", "TEAM_2"}, nullable = true)
	MakersTeam makersTeam,

	@Schema(description = "모집 단위", example = "REGULAR", allowableValues = {"REGULAR", "NEW", "REFILL"}, nullable = true)
	RecruitTypeDetail recruitTypeDetail,

	@Schema(description = "활동 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "ENDED", "DROPOUT"}, nullable = true)
	ActivityStatus activityStatus,

	@Schema(description = "거주 지역", example = "SEOUL", nullable = true)
	Region region,

	@Schema(description = "관심 도메인 목록(1~3개)", example = "[\"커머스\", \"핀테크\"]", nullable = true)
	@Size(min = 1, max = 3, message = "관심 도메인은 최소 1개부터 최대 3개까지 선택 가능합니다.")
	List<String> interestedDomains,

	@Schema(description = "직무 관련 경험 기간", example = "THREE_TO_FOUR", allowableValues = {"NONE", "ONE_TO_TWO", "THREE_TO_FOUR", "FIVE_PLUS"}, nullable = true)
	ExperiencePeriod experiencePeriod,

	@Schema(description = "멘토링 가능 여부", example = "HIGHLY_AVAILABLE", allowableValues = {"UNAVAILABLE", "CONSIDER_LATER", "AVAILABLE_BY_TOPIC", "HIGHLY_AVAILABLE"}, nullable = true)
	Availability mentoringAvailability,

	@Schema(description = "프로젝트 충원 가능 여부", example = "AVAILABLE_BY_TOPIC", allowableValues = {"UNAVAILABLE", "CONSIDER_LATER", "AVAILABLE_BY_TOPIC", "HIGHLY_AVAILABLE"}, nullable = true)
	Availability projectSupplementAvailability,

	@Schema(description = "연사 가능 여부", example = "CONSIDER_LATER", allowableValues = {"UNAVAILABLE", "CONSIDER_LATER", "AVAILABLE_BY_TOPIC", "HIGHLY_AVAILABLE"}, nullable = true)
	Availability speakerAvailability,

	@Schema(description = "경력 수준", example = "JUNIOR", allowableValues = {"UNDER_1_YEAR", "JUNIOR", "MIDDLE", "SENIOR"}, nullable = true)
	CareerLevel careerLevel,

	@Schema(description = "보유 기술", example = "Java, Spring", maxLength = 255, nullable = true)
	@Size(max = 255, message = "기술은 255자 이하로 입력해주세요.")
	String skills,

	@Schema(description = "회사", example = "젝트", maxLength = 30, nullable = true)
	@Size(max = 30, message = "회사는 30자 이하로 입력해주세요.")
	String company,

	@Schema(description = "공유 가능한 전문 주제", example = "Spring 성능 최적화", maxLength = 30, nullable = true)
	@Size(max = 30, message = "공유 가능한 전문 주제는 30자 이하로 입력해주세요.")
	String expertTopics,

	@Schema(description = "활동 증명서 번호", example = "JECT-MAKERS-001", maxLength = 20, nullable = true)
	@Size(max = 20, message = "활동 증명서 번호는 20자 이하로 입력해주세요.")
	String activityCertNumber,

	@Schema(description = "비고", example = "차기 프로젝트 참여 희망", maxLength = 100, nullable = true)
	@Size(max = 100, message = "비고는 100자 이하로 입력해주세요.")
	String memo
) {
}
