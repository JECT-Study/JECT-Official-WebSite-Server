package org.ject.support.admin.mail.config;

import org.ject.support.admin.mail.service.MailDispatchExecutionService;
import org.ject.support.admin.mail.service.MailDispatchWorkerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(prefix = "mail.dispatch.worker", name = "enabled", havingValue = "true")
public class MailDispatchWorkerConfig {

    @Bean("mailDispatchScheduler")
    public ThreadPoolTaskScheduler mailDispatchScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // ponytail: 서버당 단일 실행으로 제한한다. 처리량 근거가 생기면 제한된 병렬 실행을 검토한다.
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("MAIL-DISPATCH-");
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Bean
    public MailDispatchWorkerService mailDispatchWorkerService(MailDispatchExecutionService executionService,
            @Value("${mail.dispatch.worker.batch-size:10}") int batchSize) {
        return new MailDispatchWorkerService(executionService, batchSize);
    }
}
