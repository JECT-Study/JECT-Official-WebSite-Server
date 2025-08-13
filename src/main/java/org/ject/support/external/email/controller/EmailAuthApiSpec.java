package org.ject.support.external.email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Email", description = "이메일 API")
public interface EmailAuthApiSpec {

    @Operation(
            summary = "인증 번호 발송",
            description = "지정된 이메일로 인증 번호를 발송합니다.")
    void sendAuthEmail(@RequestParam String sendGroupCode, @RequestParam String email);
}
