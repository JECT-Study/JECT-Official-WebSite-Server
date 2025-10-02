package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    Optional<Apply> findByMember(Member member);
}
