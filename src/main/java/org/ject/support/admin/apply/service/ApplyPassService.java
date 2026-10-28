package org.ject.support.admin.apply.service;

import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_SUBMITTED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.ApplyPassRequest;
import org.ject.support.admin.apply.dto.GetAppliesRequest;
import org.ject.support.admin.apply.dto.UpdateSelectionResultsRequest;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplyPassService {

    private final AdminApplyService adminApplyService;

    // 같은 공고의 지원서를 합격 처리하고 남은 예비 번호 정리
    @Transactional
    public void passApply(ApplyPassRequest request) {
        // 지원 정보 조회
        List<Apply> applies = adminApplyService.getAppliesForPass(new GetAppliesRequest(request.applyIds()));

        // 제출 상태를 먼저 검증하고 같은 공고의 선정 결과와 예비 순서를 함께 정리
        applies.forEach(apply -> {
            if (apply.isNotSubmitted()) {
                throw new ApplyException(NOT_SUBMITTED);
            }
        });
        adminApplyService.updateSelectionResults(new UpdateSelectionResultsRequest(
                applies.getFirst().getRecruit().getId(), request.applyIds(), SelectionResult.PASSED));
    }
}
