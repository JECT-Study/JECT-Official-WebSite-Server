package org.ject.support.external.email.event;

import org.springframework.mail.javamail.MimeMessagePreparator;

public record EmailSendEvent(MimeMessagePreparator[] preparators) {
}
