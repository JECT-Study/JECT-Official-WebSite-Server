package org.ject.support.domain.recruit.service;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.ApplyPortfolioDto;
import org.ject.support.domain.recruit.dto.ApplyTemporaryResponse;

import java.util.List;
import java.util.Map;

public interface ApplyUsecase {
    ApplyTemporaryResponse getTemporaryApplication(Long memberId);

    void applyTemporary(JobFamily jobFamily,
                        Long memberId,
                        Map<String, String> answers,
                        List<ApplyPortfolioDto> portfolios);


    void changeJobFamily(Long memberId, JobFamily newJobFamily);

    void submitApplication(Long memberId,
                           JobFamily jobFamily,
                           Map<String, String> answers,
                           List<ApplyPortfolioDto> portfolios);
}
