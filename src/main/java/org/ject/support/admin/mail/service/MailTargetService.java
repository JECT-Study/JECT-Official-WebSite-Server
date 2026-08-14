package org.ject.support.admin.mail.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.repository.MailTargetQueryRepository;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailTargetService {

    private final RecruitRepository recruitRepository;
    private final MailTargetQueryRepository mailTargetQueryRepository;

    public List<MailTargetResponse> getTargets(Long recruitId, SelectionResult selectionResult) {
        if (!recruitRepository.existsById(recruitId)) {
            throw new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT);
        }
        return mailTargetQueryRepository.findTargets(recruitId, selectionResult);
    }
}
