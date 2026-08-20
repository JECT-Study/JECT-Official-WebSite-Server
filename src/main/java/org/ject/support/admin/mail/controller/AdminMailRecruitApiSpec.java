package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.ject.support.admin.mail.dto.MailRecruitResponse;

@Tag(name = "AdminMailRecruit", description = "메일 발송용 모집 공고 조회 API (어드민 전용)")
public interface AdminMailRecruitApiSpec {

    @Operation(
            summary = "메일 발송용 모집 공고 목록 조회",
            description = "메일 발송 기준으로 사용할 모집 공고를 등록일 최신순으로 조회합니다.")
    List<MailRecruitResponse> getRecruits();
}
