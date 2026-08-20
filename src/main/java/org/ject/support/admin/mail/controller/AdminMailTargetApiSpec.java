package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.dto.MailTargetSearchRequest;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "AdminMailTarget", description = "메일 발송 대상자 조회 API (어드민 전용)")
public interface AdminMailTargetApiSpec {

    @Operation(summary = "메일 발송 대상자 조회", description = "모집 공고와 선정 결과로 메일 발송 대상자를 조회합니다.")
    List<MailTargetResponse> searchTargets(@ModelAttribute @Valid MailTargetSearchRequest request);
}
