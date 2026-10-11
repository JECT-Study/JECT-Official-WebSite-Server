package org.ject.support.admin.mail.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.admin.mail.dto.MailDispatchTransitionEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailDispatchTransitionTelemetryService {

    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void recordTransition(MailDispatchTransitionEvent event) {
        // 커밋 이후만 계측하고 계측 실패가 영속 결과를 바꾸지 않도록 처리
        try {
            log.info("[MAIL_DISPATCH_STATE] jobId={} targetId={} status={} attemptCount={} nextAttemptAt={}",
                    event.dispatchJobId(), event.targetId(), event.status(), event.attemptCount(), event.nextAttemptAt());
            meterRegistryProvider.ifAvailable(registry -> registry.counter(
                    "mail.dispatch.transitions", "status", event.status().name()).increment());
        } catch (RuntimeException exception) {
            log.warn("[MAIL_DISPATCH_TELEMETRY_FAILURE] jobId={} targetId={} status={} errorType={}",
                    event.dispatchJobId(), event.targetId(), event.status(), exception.getClass().getSimpleName());
        }
    }
}
