package org.ject.support.domain.apply.domain;

/**
 * 제출된 지원을 운영팀이 평가한 결과. 지원 상태(ApplyStatus)와는 독립적인 축이다.
 */
public enum SelectionResult {
    UNDECIDED, PASSED, WAITLISTED, FAILED;

    public boolean isWaitlisted() {
        return this == WAITLISTED;
    }

    public boolean isPassed() {
        return this == PASSED;
    }
}
