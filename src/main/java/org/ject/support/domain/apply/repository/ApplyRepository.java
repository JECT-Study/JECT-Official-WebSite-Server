package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

    @Query("select a from Apply a join a.recruit r where a.applicant.id = :applicantId and r.id = :recruitId and r.startDate <= :now and r.endDate >= :now")
    Optional<Apply> findByApplicantIdAndRecruitIdInActiveRecruit(@Param("applicantId") Long applicantId,
                                                                 @Param("recruitId") Long recruitId,
                                                                 @Param("now") LocalDateTime now);

    @Query("""
            select exists (
                select 1 from Apply a join a.recruit r
                where a.applicant.id = :applicantId and r.id = :recruitId and r.startDate <= :now and r.endDate >= :now
            )
            """)
    boolean existsByApplicantIdAndRecruitIdInActiveRecruit(@Param("applicantId") Long applicantId,
                                                           @Param("recruitId") Long recruitId,
                                                           @Param("now") LocalDateTime now);

    List<Apply> findByRecruitAndStatus(Recruit recruit, ApplyStatus status);

    @Query("select a from Apply a join fetch a.applicant m where a.id = :applyId and a.status = :status")
    Optional<Apply> findByIdAndStatusWithApplicant(@Param("applyId") Long applyId, @Param("status") ApplyStatus status);

    @Query("select a from Apply a join fetch a.applicant m where a.id in :applyIds and a.status = :status")
    List<Apply> findAllByIdAndStatusWithApplicant(@Param("applyIds") List<Long> applyIds, @Param("status") ApplyStatus status);

    @Query("select count(a) from Apply a where a.status = :status")
    Long countByStatus(@Param("status") ApplyStatus status);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant JOIN FETCH a.recruit WHERE a.id IN :ids")
    List<Apply> findAllByIdWithApplicant(@Param("ids") List<Long> ids);
}
