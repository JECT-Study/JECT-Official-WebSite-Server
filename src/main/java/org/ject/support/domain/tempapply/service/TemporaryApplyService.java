package org.ject.support.domain.tempapply.service;

import java.util.Map;
import org.ject.support.domain.member.JobFamily;

public interface TemporaryApplyService {

    /**
     * 사용자의 임시 지원서를 저장<br/> 임시지원서의 양식이 지원 파트(직군)에 적절한지 판별 후 저장<br/> 임시 지원서는 덮어써지는 형태가 아닌 새로운 임시저장본이 추가로 저장되는 형태<br/>
     */
    void saveTemporaryApplication(JobFamily jobFamily, Long memberId, Map<String, String> answers);


    /**
     * 사용자의 임시 지원서를 조회<br/> 지원 파트(직군)에 따라 다른 양식을 조회 가능<br/> ex) BE 지원서를 작성하다가 PM 지원서를 작성하고, 다시 BE 지원서를 작성하려 해도 가능은 해야
     * 함<br/>
     */
    Map<String, String> findMembersRecentTemporaryApplication(JobFamily jobFamily, Long memberId);
}
