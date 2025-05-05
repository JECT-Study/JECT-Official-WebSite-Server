package org.ject.support.domain.tempapply.service;

import java.util.List;
import java.util.Map;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.ApplyPortfolioDto;
import org.ject.support.domain.recruit.dto.ApplyTemporaryResponse;

public interface TemporaryApplyService {
    /**
     * 사용자의 임시 지원서를 조회<br/>
     */
    ApplyTemporaryResponse findMembersRecentTemporaryApplication(Long memberId);

    /**
     * 사용자의 임시 지원서를 저장<br/> 임시지원서의 양식이 지원 파트(직군)에 적절한지 판별 후 저장<br/> 임시 지원서는 덮어써지는 형태가 아닌 새로운 임시저장본이 추가로 저장되는 형태<br/>
     */
    void saveTemporaryApplication(Long memberId,
                                  Map<String, String> answers,
                                  JobFamily jobFamily,
                                  List<ApplyPortfolioDto> portfolios);

    /**
     * 사용자의 임시 지원서를 모두 제거
     */
    void deleteTemporaryApplicationsByMemberId(Long memberId);

    /**
     * 활성화된 모집 기간 중 저장된 임시 지원서의 사용자 ID 중복 없이 조회
     */
    List<Long> findMemberIdsByActiveRecruits(List<Recruit> activeRecruits);
}
