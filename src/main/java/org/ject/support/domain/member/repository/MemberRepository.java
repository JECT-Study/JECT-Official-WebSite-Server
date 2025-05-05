package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
