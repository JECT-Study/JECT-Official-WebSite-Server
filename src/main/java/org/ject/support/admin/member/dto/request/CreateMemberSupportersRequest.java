package org.ject.support.admin.member.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMemberSupportersRequest(

	@NotBlank(message = "이름을 입력해주세요")
	@Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
	@Pattern(regexp = "^[^\\s]+$", message = "이름은 공백없이 입력해주세요")
	String name,

	@NotBlank(message = "전화번호를 입력해주세요")
	@Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력해주세요.")
	String phoneNumber,

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 30, message = "이메일은 30자 이하로 입력해주세요.")
	String email,

	@NotNull(message = "소속을 선택해주세요")
	MemberType memberType,

	@NotNull(message = "포지션을 선택해주세요")
	JobFamily jobFamily,

	@NotNull(message = "모집 단위를 선택해주세요")
	RecruitTypeDetail recruitTypeDetail,

	@NotNull(message = "활동 상태를 선택해주세요")
	ActivityStatus activityStatus,

	LocalDate startDate,

	LocalDate endDate,

	@Size(max = 20, message = "활동 증명서 번호는 20자 이하로 입력해주세요.")
	String activityCertNumber,

	@Size(max = 100, message = "비고는 100자 이하로 입력해주세요.")
	String memo
) implements CreateMemberRequest {

	@Override
	public List<String> interestedDomains() {
		return null;
	}

	@Override
	public Region region() {
		return null;
	}
}
