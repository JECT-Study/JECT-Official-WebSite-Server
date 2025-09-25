package org.ject.support.domain.recruit.repository;

import java.util.Optional;
import org.ject.support.domain.recruit.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, Long>, SemesterQueryRepository {
    Optional<Semester> findByName(String name);
}
