package org.ject.support.external.email.dto;

public record SendPlainTextEmailRequest(String to, String subject, String content) {
}
