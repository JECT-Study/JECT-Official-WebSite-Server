package org.ject.support.admin.mail.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일 템플릿에서 사용할 수 있는 변수 사전을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum MailVariable {

    // 공통 변수 (Client Input)
    RECRUIT_ALERT_APPLY_URL("모집 알림 신청 URL", true),
    JOIN_PROCESS_URL("합류 절차 URL", true),
    COFFEE_CHAT_RESERVATION_URL("커피챗 예약 URL", true),
    MAKERS_N_TEAM_INTRO_URL("메이커스 n팀 소개 URL", true),
    MAKERS_N_TEAM_JOIN_PROCESS_URL("메이커스 n팀 합류 절차 URL", true),
    MAKERS_N_TEAM_ACTIVITY_NOTICE_URL("메이커스 n팀 활동 유의 사항 URL", true),
    JECT_OFFICIAL_SITE_URL("젝트 공홈 URL", true),
    OPERATION_SUPPORTERS_INTRO_URL("운영 서포터즈 소개 URL", true),
    OPERATION_SUPPORTERS_JOIN_PROCESS_URL("운영 서포터즈 합류 절차 URL", true),
    EVENT_DATE_TIME("행사 일시", true),
    EVENT_LOCATION("행사 장소", true),
    DEADLINE("마감일", true),
    RECRUIT_NAME("모집명", true),

    // 개인 변수 (DB Select)
    NAME("이름", false),
    GENERATION("기수", false);

    private final String label;
    private final boolean isCommon;
}
