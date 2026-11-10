package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findByName(String name);

    @Query("select r.semester.id from Recruit r where r.id = :recruitId")
    Optional<Long> findSemesterIdByRecruitId(Long recruitId);
}
