package org.ject.support.domain.recruit.repository;

import java.util.Optional;
import org.ject.support.domain.recruit.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long>, SemesterQueryRepository {

    @Query("select s from Semester s where s.isRecruiting = true")
    Optional<Semester> findRecruitingSemester();

    Optional<Semester> findByName(String name);
}
