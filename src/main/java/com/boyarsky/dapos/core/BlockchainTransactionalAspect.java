package com.boyarsky.dapos.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class BlockchainTransactionalAspect {
    @Around("@annotation(BlockchainTransactional)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        Object proceed = joinPoint.proceed();
        return proceed;
    }
}
