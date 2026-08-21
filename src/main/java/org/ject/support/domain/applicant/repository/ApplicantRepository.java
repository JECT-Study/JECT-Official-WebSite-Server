package org.ject.support.domain.applicant.repository;

import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long>, ApplicantQueryRepository {

    Optional<Applicant> findByEmail(String email);

    Optional<Applicant> findByEmailAndSemesterId(String email, Long semesterId);

    Optional<Applicant> findByEmailAndRole(String email, Role role);

    Optional<Applicant> findByEmailAndRoleIn(String email, Collection<Role> roles);

    Optional<Applicant> findByIdAndRoleIn(Long id, Collection<Role> roles);

    boolean existsByEmail(String email);

    boolean existsByEmailAndSemesterId(String email, Long semesterId);
}
