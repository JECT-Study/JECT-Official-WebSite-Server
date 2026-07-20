package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecruitRepository extends JpaRepository<Recruit, Long> {
    @Query("SELECT r FROM Recruit r LEFT JOIN FETCH r.questions "
            + "WHERE r.startDate <= :now AND r.endDate >= :now")
    List<Recruit> findActiveRecruits(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Recruit r JOIN FETCH r.semester "
            + "WHERE r.startDate <= :now AND r.endDate >= :now "
            + "ORDER BY r.startDate ASC, r.id ASC")
    List<Recruit> findActiveRecruitments(@Param("now") LocalDateTime now);

    @Query("SELECT EXISTS(SELECT 1 FROM Recruit r "
            + "WHERE r.semester.id = :semesterId AND r.jobFamily IN :jobFamilies AND r.endDate >= now())")
    boolean existsByJobFamilyAndIsNotClosed(@Param("semesterId") Long semesterId,
                                            @Param("jobFamilies") List<JobFamily> jobFamilies);

    @Query("SELECT r FROM Recruit r WHERE r.id = :recruitId AND r.startDate <= :now AND r.endDate >= :now")
    Recruit findActiveRecruitById(@Param("recruitId") Long recruitId,
                                  @Param("now") LocalDateTime now);

    @Query("SELECT MAX(r.endDate) FROM Recruit r "
            + "WHERE r.jobFamily = :jobFamily AND r.startDate <= :now AND r.endDate >= :now")
    LocalDateTime findLatestActiveRecruitEndDateByJobFamily(@Param("jobFamily") JobFamily jobFamily,
                                                            @Param("now") LocalDateTime now);
}
