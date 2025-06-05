package org.ject.support.external.email.repository;

import org.ject.support.external.email.domain.EmailSendGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailSendGroupRepository extends JpaRepository<EmailSendGroup, Long> {
    Optional<EmailSendGroup> findByCode(String remindApply);
}
