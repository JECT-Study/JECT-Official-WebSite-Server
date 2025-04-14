package org.ject.support.domain.recruit.service;

import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;

import java.util.List;

public interface RecruitUsecase {

    /**
     * 모집 정보를 등록합니다.
     */
    void registerRecruits(List<RecruitRegisterRequest> requests);

    /**
     * 모집 정보를 수정합니다.
     */
    void updateRecruit(Long recruitId, RecruitUpdateRequest request);

    /**
     * 모집을 취소합니다.
     */
    void cancelRecruit(Long recruitId);
}
