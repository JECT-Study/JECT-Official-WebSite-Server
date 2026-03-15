package org.ject.support.admin.apply.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.apply.domain.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminApplyRepository extends JpaRepository<Apply, Long>, AdminApplyQueryRepository {

    @Query("SELECT a FROM Apply a JOIN FETCH a.member LEFT JOIN FETCH a.applicationForm WHERE a.id = :id")
    Optional<Apply> findByIdWithMember(@Param("id") Long id);

    @Query("SELECT a FROM Apply a JOIN FETCH a.member LEFT JOIN FETCH a.applicationForm WHERE a.id IN :ids")
    List<Apply> findAllByIdWithMember(@Param("ids") List<Long> ids);
}
