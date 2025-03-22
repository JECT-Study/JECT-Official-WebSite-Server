package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, Long> {
}
