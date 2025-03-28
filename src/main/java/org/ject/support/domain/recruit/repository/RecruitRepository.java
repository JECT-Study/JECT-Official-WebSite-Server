package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecruitRepository extends JpaRepository<Recruit, Long>, RecruitQueryRepository {
    @Query("SELECT r FROM Recruit r LEFT JOIN FETCH r.questions"
            + " WHERE r.startDate <= :now AND r.endDate >= :now")
    List<Recruit> findActiveRecruits(@Param("now") LocalDateTime now);
}
