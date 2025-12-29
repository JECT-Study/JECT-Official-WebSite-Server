package org.ject.support.external.email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.external.email.domain.EmailTemplate;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Email", description = "이메일 API")
public interface EmailAuthApiSpec {

    @Operation(
            summary = "인증 번호 발송",
            description = "지정된 이메일로 인증 번호를 발송합니다.")
    @Parameters({
            @Parameter(
                    name = "sendGroupCode", description = "이메일 템플릿",
                    schema = @Schema(allowableValues = {"AUTH_CODE", "PIN_RESET"}),
                    examples = {
                            @ExampleObject(name = "AUTH_CODE", summary = "이메일 인증 코드 안내", value = "AUTH_CODE"),
                            @ExampleObject(name = "PIN_RESET", summary = "PIN 재설정 인증 코드 안내", value = "PIN_RESET")
                    },
                    required = true)
    })
    void sendAuthEmail(@RequestParam EmailTemplate sendGroupCode, @RequestParam String email);
}
