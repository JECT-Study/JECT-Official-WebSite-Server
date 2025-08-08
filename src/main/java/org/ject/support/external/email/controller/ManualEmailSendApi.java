package org.ject.support.external.email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.springdoc.CustomApiResponse;
import org.ject.support.external.email.dto.SendManualBulkTemplatedEmailRequest;
import org.ject.support.external.email.dto.SendManualTemplatedEmailRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Email", description = "이메일 API")
public interface ManualEmailSendApi {

    @Operation(
            summary = "템플릿 이메일 단건 발송",
            description = "선택된 템플릿으로 단건 이메일을 발송합니다.")
    @CustomApiResponse
    void sendManualTemplatedEmail(@RequestBody SendManualTemplatedEmailRequest request);

    @Operation(
            summary = "템플릿 이메일 일괄 발송",
            description = "선택된 템플릿으로 여러 이메일을 발송합니다.")
    @CustomApiResponse
    void sendManualBulkTemplatedEmail(@RequestBody SendManualBulkTemplatedEmailRequest request);
}
