package org.ject.support.admin.apply.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminApplyRepository extends JpaRepository<Apply, Long>, AdminApplyQueryRepository {

    @Query("""
            select a from Apply a
            join fetch a.applicant p
            where a.id in :ids and a.isDeleted = false and p.isDeleted = false
            order by a.id
            """)
    List<Apply> findAppliesWithApplicant(@Param("ids") List<Long> ids);

    @Query("""
            select a from Apply a
            join fetch a.applicant p
            where a.recruit.id = :recruitId and a.id in :ids and a.isDeleted = false and p.isDeleted = false
            order by a.id
            """)
    List<Apply> findAppliesInRecruitWithApplicant(
            @Param("recruitId") Long recruitId,
            @Param("ids") List<Long> ids);

    @Query("""
            select a from Apply a
            join fetch a.applicant p
            where a.recruit.id = :recruitId and a.status = org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED
              and a.selectionResult = org.ject.support.domain.apply.domain.SelectionResult.WAITLISTED
              and a.isDeleted = false and p.isDeleted = false
            order by a.waitlistNumber asc, a.id asc
            """)
    List<Apply> findWaitlistedApplies(@Param("recruitId") Long recruitId);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant LEFT JOIN FETCH a.applicationForm WHERE a.id = :id")
    Optional<Apply> findByIdWithApplicant(@Param("id") Long id);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant LEFT JOIN FETCH a.applicationForm WHERE a.id IN :ids")
    List<Apply> findAllByIdWithApplicant(@Param("ids") List<Long> ids);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant JOIN FETCH a.recruit LEFT JOIN FETCH a.applicationForm "
            + "WHERE a.recruit.id = :recruitId AND a.id IN :applyIds")
    List<Apply> findAllByRecruitIdAndIdInWithApplicant(@Param("recruitId") Long recruitId,
                                                       @Param("applyIds") List<Long> applyIds);

    @Query("select a from Apply a join fetch a.applicant m where a.id = :applyId and a.status = :status")
    Optional<Apply> findByIdAndStatusWithApplicant(@Param("applyId") Long applyId, @Param("status") ApplyStatus status);
}
