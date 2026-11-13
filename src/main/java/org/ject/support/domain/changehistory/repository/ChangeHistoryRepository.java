package org.ject.support.domain.changehistory.repository;

import org.ject.support.domain.changehistory.domain.ChangeHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, Long> {

    @EntityGraph(attributePaths = "changeDetails")
    @Query("select ch from ChangeHistory ch where ch.domain = :domain and ch.targetId = :targetId order by ch.id desc")
    List<ChangeHistory> findAllByDomainAndTargetIdOrderByIdDesc(
        @Param("domain") String domain,
        @Param("targetId") String targetId
    );
}
