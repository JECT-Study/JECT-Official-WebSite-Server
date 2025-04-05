package org.ject.support.domain.recruit.service;

import org.ject.support.domain.recruit.dto.SemesterRegisterRequest;

public interface SemesterRegisterUsecase {
    void registerSemester(SemesterRegisterRequest request);
}
