package org.ject.support.admin.mail.service;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class MailDispatchWorkerService {

    private final MailDispatchExecutionService executionService;
    private final int batchSize;

    public MailDispatchWorkerService(MailDispatchExecutionService executionService, int batchSize) {
        if (batchSize < 1 || batchSize > 100) {
            throw new IllegalArgumentException("mail.dispatch.worker.batch-size must be between 1 and 100");
        }
        this.executionService = executionService;
        this.batchSize = batchSize;
    }

    @Scheduled(scheduler = "mailDispatchScheduler", fixedDelayString = "${mail.dispatch.worker.poll-delay:1000}")
    public void poll() {
        try {
            // 격리와 안전한 미처리 발송을 분리하고 이전 배치 종료 뒤 다음 배치를 실행한다.
            executionService.quarantineInterruptedBatch(LocalDateTime.now(), batchSize);
            executionService.executePendingBatch(batchSize);
        } catch (Exception exception) {
            log.warn("메일 worker 배치 처리 실패 errorType={}", exception.getClass().getSimpleName());
        }
    }
}
