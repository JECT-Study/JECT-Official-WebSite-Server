package org.ject.support.external.email.service;

import java.util.List;
import java.util.Map;

public interface EmailSendService {

    /**
     * 단건 templated email 전송 (template)
     */
    void sendTemplatedEmail(String sendGroupCode, String to, Map<String, String> params);

    /**
     * 대량 templated email 발송
     */
    void sendBulkTemplatedEmail(String sendGroupCode, List<String> toList, Map<String, String> params);
}
