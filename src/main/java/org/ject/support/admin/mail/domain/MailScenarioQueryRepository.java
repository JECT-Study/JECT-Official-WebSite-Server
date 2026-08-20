package org.ject.support.admin.mail.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MailScenarioQueryRepository {

    Page<MailScenario> findScenarios(MailScenarioCategory category,
                                     MailScenarioType type,
                                     Pageable pageable);
}
