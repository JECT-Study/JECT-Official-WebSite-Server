package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdatedEvent;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dev Recruit", description = "개발용 모집 관리 API")
@Slf4j
@RestController
@RequestMapping("/admin/recruits")
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class DevRecruitController {

    private final RecruitRepository recruitRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "모집 정보 강제 수정", description = "마감 여부와 관계없이 모집 정보를 강제로 수정합니다.")
    @PutMapping("/{recruitId}/force-update")
    @Transactional
    public void updateRecruit(@PathVariable Long recruitId,
                                   @RequestBody @Valid RecruitUpdateRequest request) {
        log.info("force update recruitId: {}, request: {}", recruitId, request);

        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));
        recruit.update(request.jobFamily(), request.startDate(), request.endDate());

        eventPublisher.publishEvent(new RecruitUpdatedEvent(
                recruit.getId(),
                recruit.getJobFamily(),
                recruit.getStartDate(),
                recruit.getEndDate()));
    }
}
