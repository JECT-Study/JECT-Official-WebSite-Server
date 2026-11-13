package org.ject.support.common.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.AuthenticatedApplicantIdResolver;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AuditAspectTest extends UnitTestSupport {

    @InjectMocks
    private AuditAspect auditAspect;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ChangeHistoryRecorder changeHistoryRecorder;

    @Mock
    private AuthenticatedApplicantIdResolver applicantIdResolver;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    void 작업_컨텍스트가_생성되고_작업_후_정리된다() throws Throwable {
        // given
        Audit audit = annotatedAudit("noTarget");
        when(joinPoint.proceed()).thenReturn("result");
        when(applicantIdResolver.resolveCurrentApplicantId()).thenReturn(42L);

        // when
        Object result = auditAspect.around(joinPoint, audit);

        // then
        assertThat(result).isEqualTo("result");

        ArgumentCaptor<AuditContext> captor = ArgumentCaptor.forClass(AuditContext.class);
        verify(changeHistoryRecorder).record(captor.capture());

        AuditContext recorded = captor.getValue();
        assertThat(recorded.getDomain()).isEqualTo("MEMBER");
        assertThat(recorded.getAction()).isEqualTo(ChangeActionType.UPDATE);
        assertThat(recorded.getTargetId()).isNull();
        assertThat(recorded.getActorId()).isEqualTo(42L);

        assertThat(AuditContextHolder.get()).isNull();
    }

    @Test
    void SpEL로_대상_ID가_추출된다() throws Throwable {
        // given
        Audit audit = annotatedAudit("withTarget", Long.class);
        stubSignature("withTarget", Long.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{99L});
        when(joinPoint.proceed()).thenReturn(null);

        // when
        auditAspect.around(joinPoint, audit);

        // then
        ArgumentCaptor<AuditContext> captor = ArgumentCaptor.forClass(AuditContext.class);
        verify(changeHistoryRecorder).record(captor.capture());
        assertThat(captor.getValue().getTargetId()).isEqualTo("99");
    }

    @Test
    void 비즈니스_작업_후_flush와_이력_저장이_순서대로_실행된다() throws Throwable {
        // given
        Audit audit = annotatedAudit("noTarget");
        when(joinPoint.proceed()).thenReturn(null);

        // when
        auditAspect.around(joinPoint, audit);

        // then
        InOrder inOrder = inOrder(joinPoint, entityManager, changeHistoryRecorder);
        inOrder.verify(joinPoint).proceed();
        inOrder.verify(entityManager).flush();
        inOrder.verify(changeHistoryRecorder).record(any());
    }

    @Test
    void 비즈니스_작업이_실패하면_이력을_저장하지_않고_컨텍스트를_정리한다() throws Throwable {
        // given
        Audit audit = annotatedAudit("noTarget");
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("business failure"));

        // when & then
        assertThatThrownBy(() -> auditAspect.around(joinPoint, audit))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("business failure");

        verify(changeHistoryRecorder, never()).record(any());
        assertThat(AuditContextHolder.get()).isNull();
    }

    @Test
    void 이력_저장_중_예외가_발생해도_컨텍스트는_정리된다() throws Throwable {
        // given
        Audit audit = annotatedAudit("noTarget");
        when(joinPoint.proceed()).thenReturn(null);
        doThrow(new IllegalStateException("record failure"))
            .when(changeHistoryRecorder).record(any());

        // when & then
        assertThatThrownBy(() -> auditAspect.around(joinPoint, audit))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("record failure");

        assertThat(AuditContextHolder.get()).isNull();
    }

    private Audit annotatedAudit(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = DummyService.class.getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(Audit.class);
    }

    private void stubSignature(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = DummyService.class.getDeclaredMethod(methodName, parameterTypes);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    private static class DummyService {

        @Audit(domain = "MEMBER", action = ChangeActionType.UPDATE)
        void noTarget() {
        }

        @Audit(domain = "MEMBER", action = ChangeActionType.UPDATE, targetId = "#memberId")
        void withTarget(Long memberId) {
        }
    }
}
