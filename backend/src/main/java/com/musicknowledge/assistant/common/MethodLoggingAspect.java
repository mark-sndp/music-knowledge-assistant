package com.musicknowledge.assistant.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Aspect
@Component
@Order(100)
@EnableAspectJAutoProxy
public class MethodLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodLoggingAspect.class);

    @Pointcut("execution(public * com.musicknowledge.assistant..*(..))")
    public void applicationMethods() {
    }

    @Around("applicationMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        long startedAt = System.nanoTime();
        LOGGER.atInfo()
            .addKeyValue("event", "method.entry")
            .addKeyValue("class", className)
            .addKeyValue("method", methodName)
            .log("Method entry");

        try {
            Object result = joinPoint.proceed();
            LOGGER.atInfo()
                .addKeyValue("event", "method.exit")
                .addKeyValue("class", className)
                .addKeyValue("method", methodName)
                .addKeyValue("outcome", "success")
                .addKeyValue("duration_ms", elapsedMilliseconds(startedAt))
                .log("Method exit");
            return result;
        } catch (Throwable exception) {
            LOGGER.atError()
                .addKeyValue("event", "method.exit")
                .addKeyValue("class", className)
                .addKeyValue("method", methodName)
                .addKeyValue("outcome", "error")
                .addKeyValue("duration_ms", elapsedMilliseconds(startedAt))
                .addKeyValue("exception", exception.getClass().getName())
                .log("Method exit with error", exception);
            throw exception;
        }
    }

    private long elapsedMilliseconds(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
