package com.epam.rd.autocode.assessment.appliances.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.epam.rd.autocode.assessment.appliances.service.impl.*.*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(* com.epam.rd.autocode.assessment.appliances.security.CustomUserDetailsService.loadUserByUsername(..))")
    public void authAttempt() {
    }

    @Around("serviceLayer()")
    public Object logTiming(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        String cls = jp.getTarget().getClass().getSimpleName();
        String mtd = jp.getSignature().getName();
        try {
            Object result = jp.proceed();
            long ms = System.currentTimeMillis() - start;
            if (ms > 500)
                log.warn("[SLOW] {}.{}() {}ms", cls, mtd, ms);
            else
                log.debug("[SVC] {}.{}() {}ms", cls, mtd, ms);
            return result;
        } catch (Exception ex) {
            log.error("[SVC ERR] {}.{}() threw {}: {}", cls, mtd, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    @Before("authAttempt()")
    public void logAuth(JoinPoint jp) {
        log.info("[AUTH] Attempt for: '{}'", jp.getArgs()[0]);
    }

    @AfterReturning("authAttempt()")
    public void logAuthOk(JoinPoint jp) {
        log.info("[AUTH] Success for: '{}'", jp.getArgs()[0]);
    }

    @AfterThrowing(pointcut = "authAttempt()", throwing = "ex")
    public void logAuthFail(JoinPoint jp, Throwable ex) {
        log.warn("[AUTH] FAILED for '{}': {}", jp.getArgs()[0], ex.getMessage());
    }
}
