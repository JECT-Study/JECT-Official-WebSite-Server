package org.ject.support.admin.member.dto.request;

import java.util.List;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "일반 구성원 편집 요청. 전달하지 않은 항목은 기존 값 유지")
public record EditMemberSemesterRequest(
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

	@Schema(description = "직군", example = "BE", allowableValues = {"PM", "PD", "FE", "BE", "APP"}, nullable = true)
	JobFamily jobFamily,

	@Schema(description = "모집 단위", example = "REGULAR", allowableValues = {"REGULAR", "NEW", "REFILL"}, nullable = true)
	RecruitTypeDetail recruitTypeDetail,

	@Schema(description = "활동 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "COMPLETED", "WITHDRAWN"}, nullable = true)
	ActivityStatus activityStatus,

	@Schema(description = "구성원 신분", example = "EMPLOYEE", allowableValues = {"STUDENT", "EXPECTED_GRADUATE", "JOB_SEEKER", "BETWEEN_JOBS", "EMPLOYEE"}, nullable = true)
	CareerDetails careerDetails,

	@Schema(description = "기수 ID. 전달하면 팀 소속도 함께 편집", example = "2", nullable = true)
	Long semesterId,

	@Schema(description = "팀 ID. 단독 전달 시 현재 기수의 팀으로 편집", example = "5", nullable = true)
	Long teamId,

	@Schema(description = "직무 관련 경험 기간", example = "ONE_TO_TWO", allowableValues = {"NONE", "ONE_TO_TWO", "THREE_TO_FOUR", "FIVE_PLUS"}, nullable = true)
	ExperiencePeriod experiencePeriod,

	@Schema(description = "비고", example = "일반 구성원 비고", maxLength = 100, nullable = true)
	@Size(max = 100, message = "비고는 100자 이하로 입력해주세요.")
	String memo,

	@Schema(description = "관심 도메인 목록(1~3개)", example = "[\"커머스\", \"핀테크\"]", nullable = true)
	@Size(min = 1, max = 3, message = "관심 도메인은 최소 1개부터 최대 3개까지 선택 가능합니다.")
	List<String> interestedDomains,

	@Schema(description = "거주 지역", example = "SEOUL", nullable = true)
	Region region,

	@Schema(description = "활동 증명서 번호", example = "JECT-SEMESTER-001", maxLength = 20, nullable = true)
	@Size(max = 20, message = "활동 증명서 번호는 20자 이하로 입력해주세요.")
	String certNumber,

	@Schema(description = "1차 활동 리뷰", example = "https://example.com/review/1", maxLength = 255, nullable = true)
	@Size(max = 255, message = "1차 활동 리뷰는 255자 이하로 입력해주세요.")
	String firstReview,

	@Schema(description = "2차 활동 리뷰", example = "https://example.com/review/2", maxLength = 255, nullable = true)
	@Size(max = 255, message = "2차 활동 리뷰는 255자 이하로 입력해주세요.")
	String secondReview
) {
}
