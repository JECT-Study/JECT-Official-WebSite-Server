package org.ject.support.common.security.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthenticatedMemberIdResolver;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticatedMemberIdResolver());
    }
}
