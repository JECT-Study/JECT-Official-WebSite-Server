package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitRepository extends JpaRepository<Recruit, Long>, RecruitQueryRepository {
}
