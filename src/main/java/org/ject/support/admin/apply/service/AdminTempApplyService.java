package org.ject.support.admin.apply.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
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

    @Transactional
    public void deleteTempApply(Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM));
        apply.getMember().deleteProfile();
        applyRepository.delete(apply);
    }
}
