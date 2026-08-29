package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.PreviewMailRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "AdminMailScenario", description = "메일 발송 시나리오 관리 API (어드민 전용)")
public interface AdminMailScenarioApiSpec {

    @Operation(summary = "메일 템플릿 목록 조회", description = "메일 템플릿을 구분과 타입으로 필터링하여 최신순으로 조회합니다.")
    Page<MailScenarioResponse> searchScenarios(
            @RequestParam(required = false) MailScenarioCategory category,
            @RequestParam(required = false) MailScenarioType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable);

    @Operation(summary = "시나리오 생성", description = "새로운 메일 발송 시나리오를 생성합니다.")
    MailScenarioResponse createScenario(@RequestBody @Valid MailScenarioRequest request);

    @Operation(summary = "시나리오 수정", description = "기존 메일 발송 시나리오를 수정합니다.")
    MailScenarioResponse updateScenario(@PathVariable Long scenarioId,
                                         @RequestBody @Valid MailScenarioRequest request);

    @Operation(summary = "시나리오 삭제", description = "메일 발송 시나리오를 삭제합니다.")
    void deleteScenario(@PathVariable Long scenarioId);

    @Operation(summary = "시나리오 변수 목록 조회", description = "시나리오 ID로 템플릿 변수 목록(공통/개인)을 조회합니다.")
    MailScenarioVariableResponse getVariablesByScenario(@PathVariable Long scenarioId);

    @Operation(summary = "메일 미리보기", description = "지원자와 입력 변수 기준으로 메일 제목과 본문을 미리 확인합니다.")
    MailPreviewResponse preview(@RequestBody @Valid PreviewMailRequest request);
}
