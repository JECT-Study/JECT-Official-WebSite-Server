package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
