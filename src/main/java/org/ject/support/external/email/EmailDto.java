package org.ject.support.external.email;

import com.amazonaws.services.dynamodbv2.xspec.S;
import lombok.Data;
import lombok.Getter;

public class EmailDto {

    @Data
    @Getter
    public static class SendEmailRequest {
        private String email;
    }

    @Data
    @Getter
    public static class VerifyAuthCodeRequest {
        private String email;
        private String authCode;
    }
}
