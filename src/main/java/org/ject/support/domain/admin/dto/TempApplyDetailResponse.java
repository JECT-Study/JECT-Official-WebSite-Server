package org.ject.support.domain.admin.dto;

import org.ject.support.common.util.DateTimeUtil;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.member.JobFamily;

import java.util.List;
import java.util.Map;

public record TempApplyDetailResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily memberJobFamily,
        String semesterName,
        String savedAt,
        String lastModifiedAt,
        JobFamily recruitJobFamily,
        TempApplicationFormResponse tempApplicationFormResponse
) {

    public static TempApplyDetailResponse from(
            Apply apply,
            Map<String, String> answers,
            List<ApplyPortfolioDto> portfolios
    ) {
        return new TempApplyDetailResponse(
            apply.getId(),
            apply.getMember().getName(),
            apply.getMember().getPhoneNumber(),
            apply.getMember().getEmail(),
            apply.getMember().getJobFamily(),
            apply.getRecruit().getSemester().getName(),
            DateTimeUtil.formatWithDayOfWeek(apply.getCreatedAt()),
            DateTimeUtil.formatWithDayOfWeek(apply.getUpdatedAt()),
            apply.getRecruit().getJobFamily(),
            TempApplicationFormResponse.from(answers, portfolios)
        );
    }
}
