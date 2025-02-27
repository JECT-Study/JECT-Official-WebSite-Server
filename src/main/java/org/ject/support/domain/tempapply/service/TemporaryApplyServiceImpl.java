package org.ject.support.domain.tempapply.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.tempapply.domain.TemporaryApplication;
import org.ject.support.domain.tempapply.exception.TemporaryApplicationErrorCode;
import org.ject.support.domain.tempapply.exception.TemporaryApplicationException;
import org.ject.support.domain.tempapply.repository.TemporaryApplicationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemporaryApplyServiceImpl implements TemporaryApplyService {
    private final TemporaryApplicationRepository temporaryApplicationRepository;

    @Override
    public void saveTemporaryApplication(final JobFamily jobFamily, final Long memberId,
                                         final Map<String, String> answers) {
        TemporaryApplication temporaryApplication = new TemporaryApplication(memberId.toString(), answers);
        temporaryApplicationRepository.save(temporaryApplication);
    }

    @Override
    public Map<String, String> findMembersRecentTemporaryApplication(final JobFamily jobFamily, final Long memberId) {
        TemporaryApplication latestApplication = temporaryApplicationRepository.findLatestByMemberId(memberId.toString())
                .orElseThrow(()-> new TemporaryApplicationException(TemporaryApplicationErrorCode.NOT_FOUND));

        return latestApplication.getAnswers();
    }
}
