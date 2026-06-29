package org.ject.support.domain.member.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    NOT_FOUND_MEMBER(NOT_FOUND, "MEMBER-1", "구성원을 찾을 수 없습니다."),
    ALREADY_EXIST_MEMBER(CONFLICT, "MEMBER-2", "이미 가입되어 있는 구성원입니다."),
    NOT_FOUND_SEMESTER_OF_MEMBER(NOT_FOUND, "MEMBER-3", "구성원의 기수를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(CONFLICT, "MEMBER-4", "이미 사용 중인 이메일입니다."),
    EXCEEDED_INTERESTED_DOMAINS_MAX_SIZE(PAYLOAD_TOO_LARGE, "MEMBER-5", "관심 도메인 목록의 최대 크기를 초과했습니다."),
    ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY(CONFLICT, "MEMBER-6", "해당 기수에 이미 등록된 일반 구성원 활동입니다."),
    NOT_FOUND_TEAM_OF_SEMESTER(NOT_FOUND, "MEMBER-7", "해당 기수의 팀을 찾을 수 없습니다."),
    REQUIRED_SEMESTER_FOR_TEAM_FILTER(BAD_REQUEST, "MEMBER-8", "팀 필터를 사용할 때는 기수를 함께 선택해야 합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
