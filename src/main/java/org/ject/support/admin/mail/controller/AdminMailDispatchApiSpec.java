package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobResponse;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.common.security.AuthPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "AdminMailDispatch", description = "단체 메일 발송 API (어드민 전용)")
public interface AdminMailDispatchApiSpec {

    @Operation(summary = "단체 메일 발송 작업 목록 조회", description = "관리자 본인의 발송 작업을 모집 공고와 작업 상태로 필터링해 최신순으로 조회합니다.")
    Page<MailDispatchJobResponse> searchJobs(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @RequestParam(required = false) Long recruitId,
            @RequestParam(required = false) MailDispatchJobStatus status,
            @PageableDefault(size = 10, sort = "requestedAt", direction = Direction.DESC) Pageable pageable);

    @Operation(summary = "단체 메일 발송 작업 상세 조회", description = "관리자 본인의 발송 작업 요약을 조회합니다.")
    MailDispatchJobResponse getJob(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @PathVariable Long dispatchJobId);

    @Operation(summary = "단체 메일 수신자별 결과 조회", description = "발송 작업의 수신자별 결과를 상태로 필터링해 조회합니다.")
    Page<MailDispatchTargetResponse> searchTargets(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @PathVariable Long dispatchJobId,
            @RequestParam(required = false) MailDispatchTargetStatus status,
            @PageableDefault(size = 10, sort = "id", direction = Direction.ASC) Pageable pageable);

    @Operation(summary = "단체 메일 발송", description = "선택한 제출 지원자에게 메일을 발송하고 대상별 결과를 기록합니다.")
    MailDispatchResponse sendMail(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid SendMailDispatchRequest request);
}
