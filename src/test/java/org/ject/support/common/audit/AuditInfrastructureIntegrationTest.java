package org.ject.support.common.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.List;
import org.ject.support.base.TestSupport;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.security.AuthenticatedApplicantIdResolver;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;
import org.ject.support.domain.changehistory.domain.ChangeHistory;
import org.ject.support.domain.changehistory.repository.ChangeHistoryRepository;
import org.ject.support.domain.member.Role;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({AuditDummyService.class, AuditAspect.class, ChangeHistoryRecorder.class,
    AuthenticatedApplicantIdResolver.class, AuditTransactionConfig.class, QueryDslTestConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuditInfrastructureIntegrationTest extends TestSupport {

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class AopConfig {
    }

    @Autowired
    private AuditDummyService auditDummyService;

    @Autowired
    private AuditDummyEntityRepository dummyEntityRepository;

    @Autowired
    private AuditDummyChildEntityRepository dummyChildEntityRepository;

    @MockitoSpyBean
    private ChangeHistoryRepository changeHistoryRepository;

    private static final Long ACTOR_ID = 42L;

    // 변경이력 수집은 작업자 식별이 필수라 테스트 전에 인증 컨텍스트를 세팅한다
    @BeforeEach
    void setUpAuthentication() {
        CustomUserDetails userDetails = new CustomUserDetails("audit@test.com", ACTOR_ID, Role.SEMESTER);
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDownAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("엔티티 생성 시 CREATE 변경이력이 수집된다")
    void 엔티티_생성_시_CREATE_변경이력이_수집된다() {
        // when
        Long id = auditDummyService.create("홍길동");

        // then
        List<ChangeHistory> histories =
            changeHistoryRepository.findAllByDomainAndTargetIdOrderByIdDesc("AUDIT_DUMMY", "홍길동");
        assertThat(histories).hasSize(1);

        ChangeHistory history = histories.get(0);
        assertThat(history.getActionType()).isEqualTo(ChangeActionType.CREATE);
        assertThat(history.getActorId()).isEqualTo(ACTOR_ID);
        assertThat(history.getChangeDetails()).hasSize(1);

        ChangeDetail detail = history.getChangeDetails().get(0);
        assertThat(detail.getEntityName()).isEqualTo(AuditDummyEntity.class.getSimpleName());
        assertThat(detail.getEntityId()).isEqualTo(String.valueOf(id));
        assertThat(detail.getActionType()).isEqualTo(ChangeActionType.CREATE);
        assertThat(detail.getBeforeData()).isNull();
        assertThat(detail.getAfterData()).contains("홍길동");
    }

    @Test
    @DisplayName("엔티티 수정 시 변경 전후 값이 정확하게 수집된다")
    void 엔티티_수정_시_변경_전후_값이_정확하게_수집된다() {
        // given
        Long id = auditDummyService.create("변경전이름");

        // when
        auditDummyService.rename(id, "변경후이름");

        // then
        List<ChangeHistory> histories =
            changeHistoryRepository.findAllByDomainAndTargetIdOrderByIdDesc("AUDIT_DUMMY", String.valueOf(id));
        assertThat(histories).hasSize(1);

        ChangeDetail detail = histories.get(0).getChangeDetails().get(0);
        assertThat(detail.getActionType()).isEqualTo(ChangeActionType.UPDATE);
        assertThat(detail.getBeforeData()).contains("변경전이름");
        assertThat(detail.getAfterData()).contains("변경후이름");
    }

    @Test
    @DisplayName("소프트 삭제는 DELETE로, 복구는 RESTORE로 분류된다")
    void 소프트_삭제는_DELETE로_복구는_RESTORE로_분류된다() {
        // given
        Long id = auditDummyService.create("소프트삭제대상");

        // when
        auditDummyService.softDelete(id);
        auditDummyService.restore(id);

        // then
        List<ChangeHistory> histories =
            changeHistoryRepository.findAllByDomainAndTargetIdOrderByIdDesc("AUDIT_DUMMY", String.valueOf(id));
        assertThat(histories).hasSize(2);

        ChangeDetail restoreDetail = histories.get(0).getChangeDetails().get(0);
        assertThat(restoreDetail.getActionType()).isEqualTo(ChangeActionType.RESTORE);

        ChangeDetail deleteDetail = histories.get(1).getChangeDetails().get(0);
        assertThat(deleteDetail.getActionType()).isEqualTo(ChangeActionType.DELETE);
    }

    @Test
    @DisplayName("하나의 작업에서 여러 엔티티를 변경하면 모두 수집된다")
    void 하나의_작업에서_여러_엔티티를_변경하면_모두_수집된다() {
        // given
        Long id = auditDummyService.create("부모");
        Long childId = dummyChildEntityRepository.save(AuditDummyChildEntity.create("자식")).getId();

        // when
        auditDummyService.renameBoth(id, "부모수정", childId, "자식수정");

        // then
        List<ChangeHistory> histories =
            changeHistoryRepository.findAllByDomainAndTargetIdOrderByIdDesc("AUDIT_DUMMY", String.valueOf(id));
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getChangeDetails()).hasSize(2);
        assertThat(histories.get(0).getChangeDetails())
            .extracting(ChangeDetail::getEntityName)
            .containsExactlyInAnyOrder(
                AuditDummyEntity.class.getSimpleName(),
                AuditDummyChildEntity.class.getSimpleName());
    }

    @Test
    @DisplayName("비즈니스 예외 발생 시 엔티티 변경과 이력 모두 롤백된다")
    void 비즈니스_예외_발생_시_엔티티_변경과_이력_모두_롤백된다() {
        // given
        Long id = auditDummyService.create("롤백대상");

        // when & then
        assertThatThrownBy(() -> auditDummyService.renameThenFail(id, "롤백시도"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("강제 실패");

        AuditDummyEntity entity = dummyEntityRepository.findById(id).orElseThrow();
        assertThat(entity.getName()).isEqualTo("롤백대상");

        List<ChangeHistory> histories =
            changeHistoryRepository.findAllByDomainAndTargetIdOrderByIdDesc("AUDIT_DUMMY", String.valueOf(id));
        assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName("이력 저장 실패 시 비즈니스 데이터도 함께 롤백된다")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void 이력_저장_실패_시_비즈니스_데이터도_함께_롤백된다() {
        // given
        doThrow(new RuntimeException("이력 저장 실패")).when(changeHistoryRepository).save(any());

        // when & then
        assertThatThrownBy(() -> auditDummyService.create("저장실패대상"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("이력 저장 실패");

        assertThat(dummyEntityRepository.findAll())
            .extracting(AuditDummyEntity::getName)
            .doesNotContain("저장실패대상");
    }

    @Test
    @DisplayName("작업자를 식별할 수 없으면 작업이 중단되고 이력도 남지 않는다")
    void 작업자를_식별할_수_없으면_작업이_중단된다() {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        assertThatThrownBy(() -> auditDummyService.create("인증없음"))
            .isInstanceOf(GlobalException.class);

        assertThat(dummyEntityRepository.findAll())
            .extracting(AuditDummyEntity::getName)
            .doesNotContain("인증없음");
        assertThat(changeHistoryRepository
            .findAllByDomainAndTargetIdOrderByIdDesc("AUDIT_DUMMY", "인증없음"))
            .isEmpty();
    }
}
