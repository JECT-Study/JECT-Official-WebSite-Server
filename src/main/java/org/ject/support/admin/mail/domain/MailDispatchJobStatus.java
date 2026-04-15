package org.ject.support.admin.mail.domain;

/**
 * 메일 발송 작업(Job)의 전체 진행 상태입니다.
 */
public enum MailDispatchJobStatus {
    REQUESTED,
    PROCESSING,
    COMPLETED,
    FAILED
}
