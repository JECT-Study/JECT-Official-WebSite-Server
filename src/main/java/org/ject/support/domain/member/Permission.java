package org.ject.support.domain.member;

import lombok.Getter;

@Getter
public enum Permission {
    APPLY_CREATE("지원서 생성"),
    APPLY_READ("지원서 조회"),
    APPLY_UPDATE("지원서 수정"),
    APPLY_DELETE("지원서 삭제"),

    APPLY_RESULT_FULL("지원 결과 처리 전체 관리"),

    MEMBER_CREATE("구성원 생성"),
    MEMBER_READ("구성원 조회"),
    MEMBER_UPDATE("구성원 수정"),
    MEMBER_DELETE("구성원 삭제"),

    MAIL_TEMPLATE_CREATE("메일 템플릿 생성"),
    MAIL_TEMPLATE_READ("메일 템플릿 조회"),
    MAIL_TEMPLATE_UPDATE("메일 템플릿 수정"),
    MAIL_TEMPLATE_DELETE("메일 템플릿 삭제"),

    MAIL_SEND_FULL("메일 발송 전체 관리"),

    ADMIN_ACCOUNT_FULL("관리자 계정 전체 관리");

    private final String description;

    Permission(final String description) {
        this.description = description;
    }
}
