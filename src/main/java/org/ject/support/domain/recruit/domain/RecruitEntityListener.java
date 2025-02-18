package org.ject.support.domain.recruit.domain;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.RecruitOpenedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitEntityListener {
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostPersist
    @PostUpdate
    public void postPersist(Recruit recruit) {
        if(recruit.isRecruitingPeriod()) {
            applicationEventPublisher.publishEvent(new RecruitOpenedEvent(recruit.getId()));
        }
    }
}
