package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 기한 외 접근 제한을 위한 flag caching 초기화
 */
@Component
@RequiredArgsConstructor
public class AccessPeriodInitializer implements ApplicationRunner {
    private final RecruitRepository recruitRepository;
    private final RecruitFlagService recruitFlagService;

    @Override
    public void run(final ApplicationArguments args) {
        recruitRepository.findActiveRecruits(LocalDateTime.now())
                .forEach(recruitFlagService::setRecruitFlag);
    }
}
