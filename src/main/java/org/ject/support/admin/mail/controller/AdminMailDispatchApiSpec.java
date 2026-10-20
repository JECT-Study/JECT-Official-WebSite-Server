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

    @Operation(summary = "단체 메일 발송 작업 목록 조회", description = "관리자 본인의 발송 작업을 최신순으로 조회합니다.")
    Page<MailDispatchJobResponse> searchJobs(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @RequestParam(required = false) Long recruitId,
            @RequestParam(required = false) MailDispatchJobStatus status,
            @PageableDefault(size = 10, sort = "requestedAt", direction = Direction.DESC) Pageable pageable);

    @Operation(summary = "단체 메일 발송 작업 상세 조회", description = "관리자 본인의 발송 작업 요약을 조회합니다.")
    MailDispatchJobResponse getJob(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @PathVariable Long dispatchJobId);

    @Operation(summary = "단체 메일 수신자별 결과 조회", description = """
            관리자 본인의 발송 작업에서 수신자별 결과와 시도 횟수·다음 재시도 시각·마지막 시도 오류를
            상태로 필터링해 조회합니다. Outbox 없는 과거 이력의 재시도 정보는 null입니다.
            """)
    Page<MailDispatchTargetResponse> searchTargets(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @PathVariable Long dispatchJobId,
            @RequestParam(required = false) MailDispatchTargetStatus status,
            @PageableDefault(size = 10, sort = "id", direction = Direction.ASC) Pageable pageable);

    @Operation(summary = "단체 메일 발송", description = """
            선택한 제출 지원자의 발송 의도를 저장합니다. worker 활성화 시 최종 발송을 기다리지 않고
            현재 상태를 반환하므로 작업 상세 조회로 결과를 확인합니다. 비활성 시 즉시 발송합니다.
            """)
    MailDispatchResponse sendMail(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid SendMailDispatchRequest request);
}
