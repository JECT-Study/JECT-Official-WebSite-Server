package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailDispatchExecuteResponse;
import org.ject.support.admin.mail.dto.MailDispatchFailedTargetResponse;
import org.ject.support.admin.mail.dto.MailDispatchHistoryResponse;
import org.ject.support.admin.mail.dto.MailDispatchRequest;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.MailDispatchDetailResponse;
import org.ject.support.admin.mail.dto.MailPreviewRequest;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.MailTestSendRequest;
import org.ject.support.admin.mail.dto.MailTestSendResponse;
import org.ject.support.admin.mail.service.MailDispatchService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 어드민 메일 발송 작업(미리보기/생성/실행/조회) API를 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mail-dispatches")
@Tag(name = "AdminMailDispatch", description = "메일 미리보기/발송/이력 조회 API (어드민 전용)")
public class AdminMailDispatchController {

    private final MailDispatchService mailDispatchService;

    /**
     * 요청 변수로 렌더링한 메일 제목/본문을 미리보기합니다.
     */
    @Operation(summary = "메일 미리보기", description = "시나리오/수신자/공통 변수 기준으로 렌더링 결과를 미리 확인합니다.")
    @PostMapping("/preview")
    public MailPreviewResponse preview(@RequestBody @Valid MailPreviewRequest request) {
        return mailDispatchService.preview(request);
    }

    /**
     * 테스트용 단건 메일을 실제로 발송합니다.
     */
    @Operation(summary = "테스트 메일 발송", description = "단건 렌더링 후 지정한 테스트 이메일로 실제 발송합니다.")
    @PostMapping("/test-send")
    public MailTestSendResponse testSend(@RequestBody @Valid MailTestSendRequest request) {
        return mailDispatchService.sendTestMail(request);
    }

    /**
     * 발송 작업(Job)과 대상(Target) 목록을 생성합니다.
     */
    @Operation(summary = "메일 발송 작업 생성", description = "시나리오와 수신자 목록으로 발송 작업 및 대상 목록을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MailDispatchResponse dispatch(@RequestBody @Valid MailDispatchRequest request) {
        return mailDispatchService.dispatch(request);
    }

    /**
     * 생성된 발송 작업을 즉시 실행합니다.
     */
    @Operation(summary = "메일 발송 작업 실행", description = "생성된 발송 작업을 즉시 실행하고 타겟 발송 결과를 반영합니다.")
    @PostMapping("/{dispatchJobId}/execute")
    public MailDispatchExecuteResponse execute(@PathVariable Long dispatchJobId) {
        return mailDispatchService.executeDispatch(dispatchJobId);
    }

    /**
     * 메일 발송 작업 이력을 최신순으로 조회합니다.
     */
    @Operation(summary = "메일 발송 작업 목록 조회", description = "생성된 메일 발송 작업 이력 목록을 최신순으로 조회합니다.")
    @GetMapping
    public List<MailDispatchHistoryResponse> getDispatchHistories() {
        return mailDispatchService.getDispatchHistories();
    }

    /**
     * 특정 발송 작업의 상세 상태를 조회합니다.
     */
    @Operation(summary = "메일 발송 작업 상세 조회", description = "발송 작업 단건의 상태/카운트/공통 변수를 조회합니다.")
    @GetMapping("/{dispatchJobId}")
    public MailDispatchDetailResponse getDispatchHistory(@PathVariable Long dispatchJobId) {
        return mailDispatchService.getDispatchHistory(dispatchJobId);
    }

    /**
     * 특정 발송 작업에서 실패한 대상 목록을 조회합니다.
     */
    @Operation(summary = "메일 발송 실패 대상 조회", description = "발송 작업에서 실패한 대상 목록과 사유를 조회합니다.")
    @GetMapping("/{dispatchJobId}/failed-targets")
    public List<MailDispatchFailedTargetResponse> getFailedTargets(@PathVariable Long dispatchJobId) {
        return mailDispatchService.getFailedTargets(dispatchJobId);
    }
}
