package org.ject.support.domain.tempapply.service;

import java.util.Map;
import org.ject.support.domain.member.JobFamily;

public interface TemporaryApplyService {

    //1. jobFamily를 통해 현재 기수 지원양식 id를 가져옴
    //2. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
    //3. 지원서 객체 생성
    //4. 지원서 저장
    /**
     * 사용자의 임시 지원서를 저장<br/> 임시지원서의 양식이 지원 파트(직군)에 적절한지 판별 후 저장<br/> 임시 지원서는 덮어써지는 형태가 아닌 새로운 임시저장본이 추가로 저장되는 형태<br/>
     */
    void saveTemporaryApplication(JobFamily jobFamily, Long memberId, Map<String, String> answers);


    /**
     * 사용자의 임시 지원서를 조회<br/> 지원 파트(직군)에 따라 다른 양식을 조회 가능<br/> ex) BE 지원서를 작성하다가 PM 지원서를 작성하고, 다시 BE 지원서를 작성하려 해도 가능은 해야
     * 함<br/>
     */
    Map<String, String> findMembersTemporaryApplication(JobFamily jobFamily, Long memberId);
}
