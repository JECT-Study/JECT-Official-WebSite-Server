package org.ject.support.domain.apply.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.applicant.dto.DeleteApplicantsRequest;
import org.ject.support.domain.applicant.service.ApplicantCommandService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class DevDeleteTestbotUseCase {
    private static final String TESTBOT_EMAIL = "testbot@ject.kr";
    private final ApplyService applyService;
    private final ApplicantCommandService applicantService;

    // 고정된 테스트봇의 지원정보와 계정을 하나의 트랜잭션으로 삭제
    @Transactional
    public void deleteTestbot() {
        Set<Long> applicantIds = applicantService.getApplicantIdsByEmail(TESTBOT_EMAIL);
        if (applicantIds.isEmpty()) {
            return;
        }
        DeleteApplicantsRequest request = new DeleteApplicantsRequest(applicantIds);
        applyService.deleteAppliesByApplicants(request);
        applicantService.deleteApplicants(request);
    }
}
