package org.ject.support.domain.admin.dto;

import java.util.List;
import java.util.Map;
import org.ject.support.common.util.DateTimeUtil;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.member.JobFamily;

public record SubmittedApplyDetailResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String createdAt,
        String updatedAt,
        ApplicationFormResponse applicationFormResponse
) {
    public static SubmittedApplyDetailResponse from(Apply apply,
                                                    Map<String, String> answers,
                                                    List<ApplyPortfolioDto> portfolios) {
        return new SubmittedApplyDetailResponse(
                apply.getId(),
                apply.getMember().getName(),
                apply.getMember().getPhoneNumber(),
                apply.getMember().getEmail(),
                apply.getMember().getJobFamily(),
                DateTimeUtil.formatWithDayOfWeek(apply.getCreatedAt()),
                DateTimeUtil.formatWithDayOfWeek(apply.getUpdatedAt()),
                ApplicationFormResponse.from(answers, portfolios)
        );
    }
}