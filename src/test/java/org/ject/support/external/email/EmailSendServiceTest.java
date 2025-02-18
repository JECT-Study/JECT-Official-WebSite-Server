package org.ject.support.external.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class EmailSendServiceTest {
    @Autowired
    private EmailSendService emailSendService;

    @Test
    @DisplayName("send email")
    void send_email() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        TestMailParameter parameter = new TestMailParameter("ject", "신예찬");
//        emailSendService.sendEmail("****@gmail.com", EmailTemplate.ACCEPTANCE, Json2MapSerializer.serializeAsMap(parameter));
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeSeconds());
    }

    @Getter
    @AllArgsConstructor
    static class TestMailParameter {
        private final String to;
        private final String value;
    }
}
