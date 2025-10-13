package org.ject.support.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CareerDetails {
    STUDENT("대학생(재학/휴학)"),
    EXPECTED_GRADUATE("대학 졸업 예정"),
    JOB_SEEKER("취준생"),
    BETWEEN_JOBS("이직 준비 중"),
    EMPLOYEE("재직자");

    private final String description;
}
