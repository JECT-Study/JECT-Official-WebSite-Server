package org.ject.support.admin.member.dto.request;

import java.time.LocalDate;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "운영 서포터즈 구성원 수정 요청. 전달하지 않은 항목은 기존 값 유지")
public record UpdateMemberSupportersRequest(

	@Schema(description = "이름", example = "김젝트", maxLength = 20, nullable = true)
	@Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
	@Pattern(regexp = "^[^\\s]+$", message = "이름은 공백없이 입력해주세요")
	String name,

	@Schema(description = "전화번호", example = "01012345678", nullable = true)
	@Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력해주세요.")
	String phoneNumber,

	@Schema(description = "이메일", example = "supporter@ject.kr", maxLength = 30, nullable = true)
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 30, message = "이메일은 30자 이하로 입력해주세요.")
	String email,

	@Schema(description = "직군", example = "OPS", allowableValues = {"OPS", "INFRA", "BX", "ER"}, nullable = true)
	JobFamily jobFamily,

	@Schema(description = "모집 단위", example = "REGULAR", allowableValues = {"REGULAR", "NEW", "REFILL"}, nullable = true)
	RecruitTypeDetail recruitTypeDetail,

	@Schema(description = "활동 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "ENDED", "DROPOUT"}, nullable = true)
	ActivityStatus activityStatus,

	@Schema(description = "활동 시작일", example = "2026-01-01", nullable = true)
	LocalDate startDate,

	@Schema(description = "활동 종료일", example = "2026-12-31", nullable = true)
	LocalDate endDate,

	@Schema(description = "활동 증명서 번호", example = "JECT-SUPPORTERS-001", maxLength = 20, nullable = true)
	@Size(max = 20, message = "활동 증명서 번호는 20자 이하로 입력해주세요.")
	String activityCertNumber,

	@Schema(description = "비고", example = "차기 활동 참여 희망", maxLength = 100, nullable = true)
	@Size(max = 100, message = "비고는 100자 이하로 입력해주세요.")
	String memo
) {
}
