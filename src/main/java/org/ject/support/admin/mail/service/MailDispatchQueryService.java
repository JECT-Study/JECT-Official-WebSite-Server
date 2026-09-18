package org.ject.support.admin.mail.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobResponse;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.common.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailDispatchQueryService {

    private final MailDispatchJobRepository mailDispatchJobRepository;
    private final MailDispatchTargetRepository mailDispatchTargetRepository;

    public Page<MailDispatchJobResponse> searchJobs(Long requestedByAdminId,
                                                     Long recruitId,
                                                     MailDispatchJobStatus status,
                                                     Pageable pageable) {
        Page<MailDispatchJob> page = findJobs(requestedByAdminId, recruitId, status, pageable);
        List<MailDispatchJobResponse> content = page.getContent().stream()
                .map(MailDispatchJobResponse::from)
                .toList();
        return PageResponse.from(content, pageable, page.getTotalElements());
    }

    public MailDispatchJobResponse getJob(Long requestedByAdminId, Long dispatchJobId) {
        return MailDispatchJobResponse.from(findJob(requestedByAdminId, dispatchJobId));
    }

    public Page<MailDispatchTargetResponse> searchTargets(Long requestedByAdminId,
                                                            Long dispatchJobId,
                                                            MailDispatchTargetStatus status,
                                                            Pageable pageable) {
        findJob(requestedByAdminId, dispatchJobId);
        Page<MailDispatchTarget> page = status == null
                ? mailDispatchTargetRepository.findAllByDispatchJobIdOrderByIdAsc(dispatchJobId, pageable)
                : mailDispatchTargetRepository.findAllByDispatchJobIdAndStatusOrderByIdAsc(
                        dispatchJobId, status, pageable);
        List<MailDispatchTargetResponse> content = page.getContent().stream()
                .map(MailDispatchTargetResponse::from)
                .toList();
        return PageResponse.from(content, pageable, page.getTotalElements());
    }

    private Page<MailDispatchJob> findJobs(Long requestedByAdminId,
                                            Long recruitId,
                                            MailDispatchJobStatus status,
                                            Pageable pageable) {
        if (recruitId == null && status == null) {
            return mailDispatchJobRepository.findAllByRequestedByAdminIdOrderByRequestedAtDescIdDesc(
                    requestedByAdminId, pageable);
        }
        if (recruitId != null && status == null) {
            return mailDispatchJobRepository.findAllByRequestedByAdminIdAndRecruitIdOrderByRequestedAtDescIdDesc(
                    requestedByAdminId, recruitId, pageable);
        }
        if (recruitId == null) {
            return mailDispatchJobRepository.findAllByRequestedByAdminIdAndStatusOrderByRequestedAtDescIdDesc(
                    requestedByAdminId, status, pageable);
        }
        return mailDispatchJobRepository.findAllByRequestedByAdminIdAndRecruitIdAndStatusOrderByRequestedAtDescIdDesc(
                requestedByAdminId, recruitId, status, pageable);
    }

    private MailDispatchJob findJob(Long requestedByAdminId, Long dispatchJobId) {
        return mailDispatchJobRepository.findByIdAndRequestedByAdminId(dispatchJobId, requestedByAdminId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
    }
}
