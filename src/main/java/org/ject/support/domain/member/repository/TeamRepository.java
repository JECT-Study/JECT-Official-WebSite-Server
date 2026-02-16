package org.ject.support.domain.member.repository;

import java.util.Optional;
import org.ject.support.domain.member.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByNameAndSemesterId(String name, Long semesterId);
}
