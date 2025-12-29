package org.ject.support.external.email.service;

import org.ject.support.external.email.domain.EmailTemplate;

import java.util.List;
import java.util.Map;

public interface EmailSendService {

    /**
     * 단건 templated email 전송 (template)
     */
    void sendTemplatedEmail(EmailTemplate sendGroupCode, String to, Map<String, String> params);

    /**
     * 대량 templated email 발송
     */
    void sendBulkTemplatedEmail(EmailTemplate sendGroupCode, List<String> toList, Map<String, String> params);
}
