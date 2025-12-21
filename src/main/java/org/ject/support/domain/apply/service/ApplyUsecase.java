package org.ject.support.domain.apply.service;

import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.member.JobFamily;

import java.util.List;
import java.util.Map;

public interface ApplyUsecase {
    TempApplicationFormResponse findTempApplicationForm(Long memberId);

    void saveApplicationTemporarily(Long memberId,
                                    Map<String, String> answers,
                                    List<ApplyPortfolioDto> portfolios);


    void deleteProfileAndTempApplicationForm(Long memberId);

    void submitApplication(Long memberId,
                           JobFamily jobFamily,
                           Map<String, String> answers,
                           List<ApplyPortfolioDto> portfolios);

    ApplyStatusResponse checkApplyStatus(String email);

    void saveProfile(Long memberId, ApplyProfileRequest request);
}
