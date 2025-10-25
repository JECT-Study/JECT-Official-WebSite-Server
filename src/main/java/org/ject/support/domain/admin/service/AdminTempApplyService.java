package org.ject.support.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.TempApplyDetailResponse;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return TempApplyDetailResponse.from(
                apply,
                string2MapSerializer.serializeAsMap(tempApplicationForm.getContent()),
                tempApplicationForm.getPortfolios()
                        .stream()
                        .map(ApplyPortfolioDto::from)
                        .toList());
    }
}
