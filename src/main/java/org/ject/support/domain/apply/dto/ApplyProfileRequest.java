package org.ject.support.domain.apply.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;

import java.util.List;
import org.ject.support.domain.member.Region;

public record ApplyProfileRequest(
        @NotBlank(message = "이름을 입력해주세요")
        @Pattern(regexp = "^[^\\s]+$", message = "이름은 공백없이 입력해주세요")
        String name,

        @NotBlank(message = "전화번호를 입력해주세요")
        String phoneNumber,

        @NotNull(message = "포지션을 선택해주세요")
        JobFamily jobFamily,

        @NotNull(message = "거주 지역을 선택해주세요")
        Region region,

        @NotNull(message = "지원자 신분을 선택해주세요")
        CareerDetails careerDetails,

        @NotNull(message = "직무 관련 경험 기간을 선택해주세요")
        ExperiencePeriod experiencePeriod,

        @NotEmpty(message = "관심 도메인을 최소 1개 이상 선택해주세요")
        @Size(min = 1, max = 3, message = "관심 도메인은 최소 1개부터 최대 3개까지 선택 가능합니다.")
        List<String> interestedDomains
) {
}
