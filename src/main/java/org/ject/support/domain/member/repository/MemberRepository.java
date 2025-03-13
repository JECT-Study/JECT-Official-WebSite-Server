package org.ject.support.domain.member.repository;

import java.util.Optional;
import org.ject.support.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
