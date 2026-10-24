package org.ject.support.admin.apply.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.Region;

public record EditApplyRequest(
        @NotBlank(message = "이름을 입력해주세요")
        @Pattern(regexp = "^[^\\s]+$", message = "이름은 공백없이 입력해주세요")
        String name,

        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
        String phoneNumber,

        @NotBlank @Email @Size(max = 30)
        @Pattern(regexp = "^\\S+$", message = "이메일은 빈 값이나 공백을 입력할 수 없습니다.")
        String email,

        @NotNull(message = "지원자 신분을 선택해주세요")
        CareerDetails careerDetails,

        Region region,

        ExperiencePeriod experiencePeriod,

        @NotNull
        @Size(min = 1, max = 3, message = "관심 도메인은 최소 1개부터 최대 3개까지 선택 가능합니다.")
        List<@NotBlank String> interestedDomains,

        SelectionResult selectionResult,

        @Size(max = 100)
        String note,

        @NotNull Map<String, String> answers,

        Set<@NotNull @Positive Long> deletePortfolioIds
) {
}
