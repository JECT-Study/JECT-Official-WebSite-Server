package org.ject.support.common.audit;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ject.support.common.security.AuthenticatedApplicantIdResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class AuditAspect {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final EntityManager entityManager;
    private final ChangeHistoryRecorder changeHistoryRecorder;
    private final AuthenticatedApplicantIdResolver applicantIdResolver;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        AuditContext context = new AuditContext(
            audit.domain(),
            audit.action(),
            resolveTargetId(audit, joinPoint),
            applicantIdResolver.resolveCurrentApplicantId()
        );

        AuditContextHolder.set(context);
        try {
            Object result = joinPoint.proceed();

            entityManager.flush();

            changeHistoryRecorder.record(context);

            return result;
        } finally {
            AuditContextHolder.clear();
        }
    }

    private String resolveTargetId(Audit audit, ProceedingJoinPoint joinPoint) {
        if (audit.targetId() == null || audit.targetId().isBlank()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);

        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        if (parameterNames != null) {
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < parameterNames.length; i++) {
                evaluationContext.setVariable(parameterNames[i], args[i]);
            }
        }

        Expression expression = EXPRESSION_PARSER.parseExpression(audit.targetId());
        Object value = expression.getValue(evaluationContext);
        return value == null ? null : String.valueOf(value);
    }
}
