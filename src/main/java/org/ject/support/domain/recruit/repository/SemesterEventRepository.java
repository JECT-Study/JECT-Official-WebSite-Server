package org.ject.support.domain.recruit.repository;

import java.util.List;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.domain.SemesterEventType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterEventRepository extends JpaRepository<SemesterEvent, Long> {

    List<SemesterEvent> findAllBySemesterIdAndTypeOrderByIdAsc(
            Long semesterId,
            SemesterEventType type
    );

}
