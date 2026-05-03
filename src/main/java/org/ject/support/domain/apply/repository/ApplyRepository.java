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

    boolean existsByMemberId(Long memberId);

    @Query("select a from Apply a where a.member.id = :memberId")
    Optional<Apply> findByMemberId(Long memberId);

    @Query("select a from Apply a join a.recruit r where a.member.id = :memberId and r.startDate <= :now and r.endDate >= :now")
    Optional<Apply> findByMemberIdInActiveRecruit(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Query("select a from Apply a join a.recruit r where a.member.id = :memberId and r.id = :recruitId and r.startDate <= :now and r.endDate >= :now")
    Optional<Apply> findByMemberIdAndRecruitIdInActiveRecruit(@Param("memberId") Long memberId,
                                                               @Param("recruitId") Long recruitId,
                                                               @Param("now") LocalDateTime now);

    @Query("select count(a) > 0 from Apply a join a.recruit r where a.member.id = :memberId and r.startDate <= :now and r.endDate >= :now")
    boolean existsByMemberIdInActiveRecruit(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    default boolean existsByMemberIdAndRecruitIdInActiveRecruit(Long memberId, Long recruitId, LocalDateTime now) {
        return existsByMemberIdAndRecruitIdAndRecruitStartDateLessThanEqualAndRecruitEndDateGreaterThanEqual(
                memberId, recruitId, now, now);
    }

    boolean existsByMemberIdAndRecruitIdAndRecruitStartDateLessThanEqualAndRecruitEndDateGreaterThanEqual(
            Long memberId, Long recruitId, LocalDateTime startDate, LocalDateTime endDate);

    List<Apply> findByRecruitAndStatus(Recruit recruit, ApplyStatus status);

    @Query("select a from Apply a join fetch a.member m where a.id = :applyId and a.status = :status")
    Optional<Apply> findByIdAndStatusWithMember(@Param("applyId") Long applyId, @Param("status") ApplyStatus status);

    @Query("select a from Apply a join fetch a.member m where a.id in :applyIds and a.status = :status")
    List<Apply> findAllByIdAndStatusWithMember(@Param("applyIds") List<Long> applyIds, @Param("status") ApplyStatus status);

    @Query("select count(a) from Apply a where a.status = :status")
    Long countByStatus(@Param("status") ApplyStatus status);

    @Query("SELECT a FROM Apply a JOIN FETCH a.member WHERE a.id IN :ids")
    List<Apply> findAllByIdWithMember(@Param("ids") List<Long> ids);
}
