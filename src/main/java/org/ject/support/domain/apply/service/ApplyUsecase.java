package org.ject.support.domain.apply.service;

import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;

import java.util.List;
import java.util.Map;

public interface ApplyUsecase {
    TempApplicationFormResponse findTempApplicationForm(Long memberId, Long recruitId);

    void saveApplicationTemporarily(Long memberId,
                                    Long recruitId,
                                    Map<String, String> answers,
                                    List<ApplyPortfolioDto> portfolios);


    void deleteProfileAndTempApplicationForm(Long memberId, Long recruitId);

    void submitApplication(Long memberId,
                           Long recruitId,
                           Map<String, String> answers,
                           List<ApplyPortfolioDto> portfolios);

    ApplyStatusResponse checkApplyStatus(Long memberId, Long recruitId);

    void saveProfile(Long memberId, Long recruitId, ApplyProfileRequest request);
}
