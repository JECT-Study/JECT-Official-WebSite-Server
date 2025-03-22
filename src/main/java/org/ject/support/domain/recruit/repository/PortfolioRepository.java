package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
