package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.dto.QuestionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionQueryRepository {

    List<QuestionResponse> findByRecruitIdOfActiveRecruit(LocalDateTime now, Long recruitId);
}
