package org.ject.support.common.audit;

import org.ject.support.domain.changehistory.domain.ChangeActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 변경이력 수집이 필요한 서비스 메서드에 붙이는 어노테이션
// 작업자를 식별할 수 없으면 이력을 남기지 않고 작업이 실패한다.
// 스케줄러, 배치처럼 SecurityContext가 없는 호출에는 적용 여부를 먼저 확인한다.
// 벌크 UPDATE/DELETE, executeUpdate(), 네이티브 쿼리는 영속 컨텍스트를 거치지 않아 변경이력이 수집되지 않는다. 자세한 정책은 docs/CODE_CONVENTIONS.md 참고
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    String domain();

    ChangeActionType action();

    String targetId() default "";
}
