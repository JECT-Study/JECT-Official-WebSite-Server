package org.ject.support.common.springdoc;

import org.ject.support.common.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorResponse {
    Class<? extends ErrorCode> value();

    int code();

    String name();

    String description() default "";
}
