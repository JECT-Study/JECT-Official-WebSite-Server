package org.ject.support.admin.apply.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminApplyRepository extends JpaRepository<Apply, Long>, AdminApplyQueryRepository {

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant LEFT JOIN FETCH a.applicationForm WHERE a.id = :id")
    Optional<Apply> findByIdWithApplicant(@Param("id") Long id);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant LEFT JOIN FETCH a.applicationForm WHERE a.id IN :ids")
    List<Apply> findAllByIdWithApplicant(@Param("ids") List<Long> ids);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant JOIN FETCH a.recruit LEFT JOIN FETCH a.applicationForm "
            + "WHERE a.recruit.id = :recruitId AND a.id IN :applyIds")
    List<Apply> findAllByRecruitIdAndIdInWithApplicant(@Param("recruitId") Long recruitId,
                                                       @Param("applyIds") List<Long> applyIds);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE VERSIONED Apply a SET a.isDeleted = true, "
            + "a.selectionResult = org.ject.support.domain.apply.domain.SelectionResult.UNDECIDED, "
            + "a.waitlistNumber = null WHERE a.id IN :ids")
    void deleteAllByIds(@Param("ids") Iterable<Long> ids);

    @Query("select a from Apply a join fetch a.applicant m where a.id = :applyId and a.status = :status")
    Optional<Apply> findByIdAndStatusWithApplicant(@Param("applyId") Long applyId, @Param("status") ApplyStatus status);
}
