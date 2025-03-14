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
    public Map<String, String> findMembersRecentTemporaryApplication(final Long memberId) {
        TemporaryApplication latestApplication =
                temporaryApplicationRepository.findLatestByMemberId(memberId.toString())
                        .orElseThrow(() -> new TemporaryApplicationException(TemporaryApplicationErrorCode.NOT_FOUND));

        return latestApplication.getAnswers();
    }

    @Override
    public void saveTemporaryApplication(final Long memberId,
                                         final Map<String, String> answers,
                                         final JobFamily jobFamily) {
        TemporaryApplication temporaryApplication =
                new TemporaryApplication(memberId.toString(), answers, jobFamily.name());
        temporaryApplicationRepository.save(temporaryApplication);
    }

    @Override
    public boolean hasSameJobFamilyWithRecentTemporaryApplication(final Long memberId, final JobFamily jobFamily) {
        TemporaryApplication temporaryApplication = temporaryApplicationRepository.findLatestByMemberId(memberId.toString())
                .orElseThrow(() -> new TemporaryApplicationException(TemporaryApplicationErrorCode.NOT_FOUND));
        return temporaryApplication.isSameJobFamily(jobFamily);
    }

    @Override
    public void deleteTemporaryApplicationsByMemberId(final Long memberId) {
        temporaryApplicationRepository.deleteByMemberId(memberId.toString());
    }
}
