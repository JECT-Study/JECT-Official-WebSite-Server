package org.ject.support.admin.mail.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailDispatchOutboxClaim;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailDispatchClaimService {

    private final MailDispatchOutboxRepository mailDispatchOutboxRepository;

    // 호출자의 트랜잭션과 분리해 외부 발송 전에 claim 커밋을 완료한다.
    public Optional<MailDispatchOutboxClaim> claim(Long outboxId, LocalDateTime now, Duration leaseDuration) {
        Objects.requireNonNull(now);
        Objects.requireNonNull(leaseDuration);
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be positive");
        }
        return mailDispatchOutboxRepository.findByIdForUpdate(outboxId)
                .filter(outbox -> outbox.claim(UUID.randomUUID().toString(), now, now.plus(leaseDuration)))
                .map(MailDispatchOutboxClaim::from);
    }

    public boolean quarantineExpired(Long outboxId, LocalDateTime now) {
        return mailDispatchOutboxRepository.findByIdForUpdate(outboxId)
                .map(outbox -> outbox.quarantineExpired(now))
                .orElse(false);
    }
}
