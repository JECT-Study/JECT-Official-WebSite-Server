package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, Long> {

    @Query(value = "SELECT EXISTS(" +
            "SELECT 1 FROM application_form af " +
            "LEFT JOIN apply a on af.apply_id = a.id " +
            "JOIN recruit r on a.recruit_id = r.id " +
            "WHERE r.start_date <= :now and r.end_date >= :now and a.applicant_id = :applicantId)", nativeQuery = true)
    boolean existsByApplicantId(@Param("applicantId") Long applicantId,
                                @Param("now") LocalDateTime now);
}
