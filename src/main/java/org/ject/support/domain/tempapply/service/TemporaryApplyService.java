package org.ject.support.domain.tempapply.service;

import java.util.Map;
import org.ject.support.domain.member.JobFamily;

public interface TemporaryApplyService {
    /**
     * 사용자의 임시 지원서를 조회<br/>
     */
    Map<String, String> findMembersRecentTemporaryApplication(Long memberId);

    /**
     * 사용자의 임시 지원서를 저장<br/> 임시지원서의 양식이 지원 파트(직군)에 적절한지 판별 후 저장<br/> 임시 지원서는 덮어써지는 형태가 아닌 새로운 임시저장본이 추가로 저장되는 형태<br/>
     */
    void saveTemporaryApplication(Long memberId, Map<String, String> answers, JobFamily jobFamily);
}
