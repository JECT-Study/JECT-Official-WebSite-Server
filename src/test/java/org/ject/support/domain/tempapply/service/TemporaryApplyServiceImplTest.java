package org.ject.support.domain.tempapply.service;

import org.assertj.core.api.Assertions;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.tempapply.domain.TemporaryApplication;
import org.ject.support.domain.tempapply.repository.TemporaryApplicationRepository;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;

@IntegrationTest
@Transactional
class TemporaryApplyServiceImplTest {

    @Autowired
    TemporaryApplyService temporaryApplyService;

    @Autowired
    TemporaryApplicationRepository temporaryApplicationRepository;

    @Autowired
    RecruitRepository recruitRepository;

    @AfterEach
    void tearDown() {
        temporaryApplicationRepository.deleteAll();
    }

    @Test
    @DisplayName("활성화된 모집 기간 중 저장된 임시 지원서의 사용자 ID 중복 없이 조회")
    void find_member_ids_by_active_recruits() {
        // given
        temporaryApplicationRepository.save(createTemporaryApplication("1", Map.of(), "BE"));
        temporaryApplicationRepository.save(createTemporaryApplication("1", Map.of(), "BE"));
        temporaryApplicationRepository.save(createTemporaryApplication("2", Map.of(), "FE"));
        temporaryApplicationRepository.save(createTemporaryApplication("3", Map.of(), "FE"));
        temporaryApplicationRepository.save(createTemporaryApplication("4", Map.of(), "BE"));
        temporaryApplicationRepository.save(createTemporaryApplication("4", Map.of(), "BE"));
        temporaryApplicationRepository.save(createTemporaryApplication("4", Map.of(), "BE"));
        temporaryApplicationRepository.save(createTemporaryApplication("5", Map.of(), "PD"));

        // when
        List<Long> result = temporaryApplyService
                .findMemberIdsByActiveRecruits(List.of(getRecruit(PM), getRecruit(PD), getRecruit(FE), getRecruit(BE)));

        // then
        Assertions.assertThat(result).hasSize(5);
    }

    private Recruit getRecruit(JobFamily jobFamily) {
        return Recruit.builder()
                .semesterId(1L)
                .jobFamily(jobFamily)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
    }

    private TemporaryApplication createTemporaryApplication(String memberId,
                                                            Map<String, String> answers,
                                                            String jobFamily) {
        return new TemporaryApplication(memberId, answers, jobFamily, List.of());
    }
}