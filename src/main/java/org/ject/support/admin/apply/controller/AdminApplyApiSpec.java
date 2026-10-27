package org.ject.support.admin.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.EditApplyRequest;
import org.ject.support.admin.apply.dto.EditWaitlistOrderRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyResponse;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.admin.apply.dto.UpdateSelectionResultsRequest;
import org.ject.support.admin.apply.dto.WaitlistOrderResponse;
import org.ject.support.admin.apply.dto.WaitlistOrderSearchCondition;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin Apply", description = "[관리자] 지원서 관리 API")
public interface AdminApplyApiSpec {

    @Operation(
            summary = "제출완료 지원서 목록 조회",
            description = "모집 공고별 제출완료 지원서를 조회합니다. 지원자 신분과 선정 결과를 다중 필터링할 수 있습니다.")
    CursorPageResponse<SubmittedApplyResponse> searchSubmittedApplies(
            @Valid @ModelAttribute SubmittedApplySearchCondition condition);

    @Operation(
            summary = "임시저장 지원서 목록 조회",
            description = "모집 공고별 임시저장 지원서를 조회합니다. 지원자 신분을 다중 필터링할 수 있습니다.")
    CursorPageResponse<TempSavedApplyResponse> searchTempSavedApplies(
            @Valid @ModelAttribute TempSavedApplySearchCondition condition);

    @Operation(
            summary = "예비 번호 조정 목록 조회",
            description = "공고별 예비 번호순으로 조회합니다. 전체 인원을 포함하며 커서는 마지막 예비 번호입니다.")
    CursorPageResponse<WaitlistOrderResponse> searchWaitlistOrders(
            @Valid @ModelAttribute WaitlistOrderSearchCondition condition);

    @Operation(
            summary = "지원서 상세 조회",
            description = "전달한 ID에 해당하는 지원서의 상세 정보를 조회합니다.")
    AdminApplyDetailResponse findApply(@PathVariable Long applyId,
                                       @RequestParam(required = false) ApplyStatus applyStatus);

    @Operation(
            summary = "지원서 수정",
            description = "수정 가능한 값을 요청 내용으로 교체합니다. 선정 결과는 제출완료만 지정하며, 포트폴리오는 전달한 ID만 삭제합니다.")
    void editApply(@PathVariable Long applyId,
                   @RequestBody @Valid EditApplyRequest request);

    @Operation(
            summary = "지원자 선정 결과 일괄 변경",
            description = "같은 공고의 제출완료 지원자에게 하나의 선정 결과를 적용합니다.")
    void updateSelectionResults(@RequestBody @Valid UpdateSelectionResultsRequest request);

    @Operation(summary = "예비 번호 조정", description = "현재 예비 합격자 전체 ID를 원하는 순서로 변경합니다.")
    int editWaitlistOrder(@RequestBody @Valid EditWaitlistOrderRequest request);

    @Operation(
            summary = "지원서 삭제",
            description = "지원서와 연결된 지원자를 함께 소프트 삭제합니다. 재지원 시 이메일 인증부터 다시 진행해야 합니다.")
    void deleteApply(@PathVariable Long applyId);

    @Operation(
            summary = "지원서 다수 삭제",
            description = "조회된 지원서와 연결된 지원자를 함께 소프트 삭제하고 실제 삭제한 지원서 수를 반환합니다. "
                    + "중복 ID는 허용하지 않으며 존재하지 않거나 이미 삭제된 지원서는 제외합니다. "
                    + "재지원 시 이메일 인증부터 다시 진행해야 합니다.")
    int deleteSubmittedApplies(@RequestBody @Valid SubmittedApplyBulkDeleteRequest request);
}
