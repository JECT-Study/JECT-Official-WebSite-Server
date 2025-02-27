package org.ject.support.testconfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAuthenticatedUserSecurityContextFactory.class)
public @interface AuthenticatedUser {
    long memberId() default 1L;
    String email() default "test1@gmail.com";
}
