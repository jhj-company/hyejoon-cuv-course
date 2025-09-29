package org.hyejoon.cuvcourse.global.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final LettuceLockFacade lettuceLockFacade;

    @Around("@annotation(org.hyejoon.cuvcourse.global.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock = signature.getMethod()
            .getAnnotation(DistributedLock.class);
        String key = createKey(signature.getParameterNames(), joinPoint.getArgs(),
            distributedLock.key());

        long waitTime = distributedLock.waitTime();
        while (!lettuceLockFacade.acquireLock(key, distributedLock.leaseTime()) && waitTime > 0) {
            Thread.sleep(100);
            waitTime -= 100;
        }

        if (waitTime <= 0) {
            log.warn("Failed to acquire lock for key: {}", key);
            throw new BusinessException(CourseExceptionEnum.LOCK_ACQUISITION_FAILED);
        }

        log.info("Acquired lock for key: {}", key);
        try {
            return joinPoint.proceed();
        } finally {
            lettuceLockFacade.releaseLock(key);
            log.info("Released lock for key: {}", key);
        }
    }

    private String createKey(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }

}
