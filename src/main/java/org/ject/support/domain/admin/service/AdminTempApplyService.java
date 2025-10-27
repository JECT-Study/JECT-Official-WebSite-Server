package org.ject.support.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.admin.dto.TempSavedApplyCountResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.TempApplyDetailResponse;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTempApplyService {

    private final ApplyRepository applyRepository;
    private final String2MapSerializer string2MapSerializer;

    public TempApplyDetailResponse getTempApplyDetail(Long tempApplyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(tempApplyId, Apply.Status.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM));

        ApplicationForm tempApplicationForm = apply.getApplicationForm();
        // tempApplicationForm.getContent() 값이 없을 경우 엔 티티 오류가 발생할 수 있으므로 방어적 코드 작성
        return TempApplyDetailResponse.from(
                apply,
                tempApplicationForm == null ? Map.of() : string2MapSerializer.serializeAsMap(tempApplicationForm.getContent()),
                tempApplicationForm == null ? List.of() : tempApplicationForm.getPortfolios()
                        .stream()
                        .map(ApplyPortfolioDto::from)
                        .toList());
    }

    public TempSavedApplyCountResponse getTempSavedApplyCount() {
        Long count = applyRepository.countByStatus(Apply.Status.TEMP_SAVED);
        return new TempSavedApplyCountResponse(count);
    }

    // 임시 저장된 지원서 삭제
    @Transactional
    public void deleteTempApply(Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, Apply.Status.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
        deleteProfileAndApplicationForm(apply);
    }

    private void deleteProfileAndApplicationForm(final Apply apply) {
        //  제출된 지원서 제거
        apply.deleteApplicationForm();
        // 프로필 제거
        Member applicant = apply.getMember();
        applicant.deleteProfile();
    }
}
