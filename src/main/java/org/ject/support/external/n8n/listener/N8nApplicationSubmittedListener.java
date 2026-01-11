package org.ject.support.external.n8n.listener;

import lombok.RequiredArgsConstructor;
import org.ject.support.external.n8n.event.ApplicationSubmittedEvent;
import org.ject.support.external.n8n.service.N8nApplyService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Admin Page가 생기기 전 까지 n8n으로 지원서 제출 알림을 보냄
 *  - Admin Page가 생기면 해당 기능은 제거될 예정
 */
@Component
@RequiredArgsConstructor
public class N8nApplicationSubmittedListener {

    private final N8nApplyService n8nApplyService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ApplicationSubmittedEvent event) {
        n8nApplyService.sendToN8n(event.applyId());
    }
}
