package org.ject.support.domain.apply.service;

import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;

import java.util.List;
import java.util.Map;

public interface ApplyUsecase {
    TempApplicationFormResponse findTempApplicationForm(Long applicantId, Long recruitId);

    void saveApplicationTemporarily(Long applicantId,
                                    Long recruitId,
                                    Map<String, String> answers,
                                    List<ApplyPortfolioDto> portfolios);


    void deleteProfileAndTempApplicationForm(Long applicantId, Long recruitId);

    void submitApplication(Long applicantId,
                           Long recruitId,
                           Map<String, String> answers,
                           List<ApplyPortfolioDto> portfolios);

    ApplyStatusResponse checkApplyStatus(Long applicantId, Long recruitId);

    void saveProfile(Long applicantId, Long recruitId, ApplyProfileRequest request);
}
