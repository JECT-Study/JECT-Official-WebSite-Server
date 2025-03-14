package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QuestionResponse;

import java.time.LocalDate;
import java.util.List;

public interface QuestionQueryRepository {

    List<QuestionResponse> findByJobFamilyOfActiveRecruit(LocalDate currentDate, JobFamily jobFamily);
}
