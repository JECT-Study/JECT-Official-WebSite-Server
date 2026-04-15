package org.ject.support.admin.mail.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 메일 발송 대상(Target) 조회를 담당하는 리포지토리입니다.
 */
public interface MailDispatchTargetRepository extends JpaRepository<MailDispatchTarget, Long> {

    /**
     * 특정 작업의 대상 목록을 생성 순서대로 조회합니다.
     */
    List<MailDispatchTarget> findAllByJobIdOrderByIdAsc(Long jobId);

    /**
     * 여러 작업에 속한 대상 목록을 한 번에 조회합니다.
     */
    List<MailDispatchTarget> findAllByJobIdIn(List<Long> jobIds);

    /**
     * 특정 작업에서 특정 상태의 대상만 조회합니다.
     */
    List<MailDispatchTarget> findAllByJobIdAndStatusOrderByIdAsc(Long jobId, MailDispatchTargetStatus status);
}
