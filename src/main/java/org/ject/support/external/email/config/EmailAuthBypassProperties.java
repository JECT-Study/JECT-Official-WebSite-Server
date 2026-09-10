package org.ject.support.external.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "email-auth.bypass")
public record EmailAuthBypassProperties(boolean enabled, String email, String code) {

    public boolean isEnabledFor(String targetEmail) {
        return enabled && StringUtils.hasText(email) && StringUtils.hasText(code)
                && email.equalsIgnoreCase(targetEmail);
    }
}
