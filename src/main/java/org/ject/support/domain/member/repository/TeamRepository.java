package org.ject.support.domain.member.repository;

import java.util.List;

import org.ject.support.domain.member.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

	@Query("select t.id from Team t where t.semesterId = :semesterId")
	List<Long> findIdsBySemesterId(@Param("semesterId") Long semesterId);
}
