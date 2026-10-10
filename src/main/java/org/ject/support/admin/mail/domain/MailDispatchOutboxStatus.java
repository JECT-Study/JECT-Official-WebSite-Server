package org.ject.support.admin.mail.domain;

public enum MailDispatchOutboxStatus {
    CANCELLED,
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    UNKNOWN
}
