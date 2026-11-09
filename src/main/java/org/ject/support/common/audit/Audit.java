package org.ject.support.common.audit;

import org.ject.support.domain.changehistory.domain.ChangeActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 변경이력 수집이 필요한 서비스 메서드에 붙이는 어노테이션
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    String domain();

    ChangeActionType action();

    String targetId() default "";
}
