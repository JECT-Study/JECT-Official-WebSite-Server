package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);

	// 삭제된 이력을 포함하여 조회
	@Query(value = "SELECT * FROM member WHERE email = :email", nativeQuery = true)
	Optional<Member> findByEmailIncludingDeleted(@Param("email") String email);
}
