package org.ject.support.admin.mail.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 메일 발송 작업(Job) 조회를 담당하는 리포지토리입니다.
 */
public interface MailDispatchJobRepository extends JpaRepository<MailDispatchJob, Long> {

    /**
     * 시나리오를 함께 로딩해 발송 작업 목록을 최신순으로 조회합니다.
     */
    @Query("select job from MailDispatchJob job join fetch job.scenario order by job.id desc")
    List<MailDispatchJob> findAllWithScenarioOrderByIdDesc();

    /**
     * 시나리오를 함께 로딩해 발송 작업 단건을 조회합니다.
     */
    @Query("select job from MailDispatchJob job join fetch job.scenario where job.id = :dispatchJobId")
    Optional<MailDispatchJob> findByIdWithScenario(@Param("dispatchJobId") Long dispatchJobId);
}
