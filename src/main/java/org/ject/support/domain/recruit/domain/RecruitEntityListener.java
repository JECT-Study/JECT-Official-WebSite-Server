package org.ject.support.domain.recruit.domain;

import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.RecruitSavedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitEntityListener {
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostPersist
    public void postPersist(Recruit recruit) {
        applicationEventPublisher.publishEvent(new RecruitSavedEvent(recruit.getId()));
    }
}
