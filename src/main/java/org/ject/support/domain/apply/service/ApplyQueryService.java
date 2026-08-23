package org.ject.support.domain.apply.service;

import java.util.Optional;

public interface ApplyQueryService {

    Optional<Long> getRecruitIdByApplicantId(Long applicantId);
}
