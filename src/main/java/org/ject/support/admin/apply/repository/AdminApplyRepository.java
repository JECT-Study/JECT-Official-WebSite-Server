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

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Apply a SET a.isDeleted = true WHERE a.id IN :ids")
    void deleteAllByIds(@Param("ids") Iterable<Long> ids);

    @Query("select a from Apply a join fetch a.applicant m where a.id = :applyId and a.status = :status")
    Optional<Apply> findByIdAndStatusWithApplicant(@Param("applyId") Long applyId, @Param("status") ApplyStatus status);
}
