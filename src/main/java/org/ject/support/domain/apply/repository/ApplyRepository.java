package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

    @Query("select a from Apply a where a.member.id = :memberId")
    Optional<Apply> findByMemberId(Long memberId);

    List<Apply> findByRecruitAndStatus(Recruit recruit, Apply.Status status);

    @Query("select a from Apply a join fetch a.member m where a.id = :applyId and a.status = :status")
    Optional<Apply> findByIdAndStatusWithMember(@Param("applyId") Long applyId, @Param("status") Apply.Status status);

    @Query("select a from Apply a join fetch a.member m where a.id in :applyIds and a.status = :status")
    List<Apply> findAllByIdAndStatusWithMember(@Param("applyIds") List<Long> applyIds, @Param("status") Apply.Status status);
}
