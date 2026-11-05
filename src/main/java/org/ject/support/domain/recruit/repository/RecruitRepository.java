package org.ject.support.domain.recruit.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitRepository extends JpaRepository<Recruit, Long>, RecruitQueryRepository {

    @Query("SELECT r FROM Recruit r JOIN FETCH r.semester WHERE r.id = :recruitId")
    Optional<Recruit> findByIdWithSemester(@Param("recruitId") Long recruitId);

    @Query("SELECT r FROM Recruit r LEFT JOIN FETCH r.questions "
            + "WHERE r.startDate <= :now AND r.endDate >= :now")
    List<Recruit> findActiveRecruits(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Recruit r JOIN FETCH r.semester "
            + "WHERE r.startDate <= :now AND r.endDate >= :now "
            + "ORDER BY r.startDate ASC, r.id ASC")
    List<Recruit> findActiveRecruitments(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Recruit r JOIN FETCH r.semester "
            + "ORDER BY r.createdAt DESC, r.id DESC")
    List<Recruit> findAllForMailDispatch();

    @Query("""
            SELECT COUNT(r) > 0 FROM Recruit r LEFT JOIN r.semester s
            WHERE r.status = org.ject.support.domain.recruit.domain.RecruitStatus.OPEN
              AND r.recruitType = :recruitType AND r.jobFamily = :jobFamily
              AND ((:semesterId IS NULL AND s.id IS NULL) OR s.id = :semesterId)
              AND (:excludedId IS NULL OR r.id <> :excludedId)
            """)
    boolean existsDuplicateRecruit(@Param("recruitType") RecruitType recruitType,
                                   @Param("jobFamily") JobFamily jobFamily,
                                   @Param("semesterId") Long semesterId,
                                   @Param("excludedId") Long excludedId);

    @Query("SELECT r FROM Recruit r WHERE r.id = :recruitId AND r.startDate <= :now AND r.endDate >= :now")
    Recruit findActiveRecruitById(@Param("recruitId") Long recruitId,
                                  @Param("now") LocalDateTime now);

    @Query("SELECT MAX(r.endDate) FROM Recruit r "
            + "WHERE r.jobFamily = :jobFamily AND r.startDate <= :now AND r.endDate >= :now")
    LocalDateTime findLatestActiveRecruitEndDateByJobFamily(@Param("jobFamily") JobFamily jobFamily,
                                                            @Param("now") LocalDateTime now);
}
