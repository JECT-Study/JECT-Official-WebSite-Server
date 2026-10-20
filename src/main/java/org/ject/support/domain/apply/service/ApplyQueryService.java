package org.ject.support.domain.apply.service;

import org.ject.support.domain.apply.dto.ApplyEligibilityFacts;

import java.util.List;
import java.util.Optional;

public interface ApplyQueryService {

    Optional<Long> getRecruitIdByApplicantId(Long applicantId);

    List<ApplyEligibilityFacts> findCurrentEligibilityFactsByApplyIds(List<Long> applyIds);
}
