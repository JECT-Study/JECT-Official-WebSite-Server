package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM ApplicationForm a " +
            "LEFT JOIN a.recruit r " +
            "WHERE r.startDate <= :now and r.endDate >= :now and a.member.id = :memberId)")
    boolean existsByMemberId(@Param("memberId") Long memberId,
                             @Param("now") LocalDateTime now);
}
