package org.ject.support.admin.mail.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 메일 시나리오 조회/중복 검증을 담당하는 리포지토리입니다.
 */
@Repository
public interface MailScenarioRepository extends JpaRepository<MailScenario, Long> {

    boolean existsByScenarioCode(String scenarioCode);

    boolean existsByScenarioCodeAndIdNot(String scenarioCode, Long id);
}
