package org.ject.support.domain.recruit.repository;

import java.time.LocalDate;
import java.util.List;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitRepository extends JpaRepository<Recruit, Long>, RecruitQueryRepository {
    @Query("SELECT r FROM Recruit r LEFT JOIN FETCH r.questions"
           + " WHERE r.startDate <= :currentDate AND r.endDate >= :currentDate")
    List<Recruit> findActiveRecruits(@Param("currentDate") LocalDate currentDate);
}
