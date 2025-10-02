package org.ject.support.domain.apply.service;

import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyTemporaryResponse;
import org.ject.support.domain.member.JobFamily;

import java.util.List;
import java.util.Map;

public interface ApplyUsecase {
    ApplyTemporaryResponse getTemporaryApplication(Long memberId);

    void applyTemporary(JobFamily jobFamily,
                        Long memberId,
                        Map<String, String> answers,
                        List<ApplyPortfolioDto> portfolios);


    void deleteTemporaryApplications(Long memberId);

    void submitApplication(Long memberId,
                           JobFamily jobFamily,
                           Map<String, String> answers,
                           List<ApplyPortfolioDto> portfolios);

    boolean checkApplySubmit(Long memberId);
}
