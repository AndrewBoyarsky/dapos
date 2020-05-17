package com.boyarsky.dapos.core.repository.aop;

import com.boyarsky.dapos.core.repository.XodusRepoContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class TransactionalAspect {
    private final XodusRepoContext context;

    @Autowired
    public TransactionalAspect(XodusRepoContext context) {
        this.context = context;
    }

    @Pointcut("@annotation(com.boyarsky.dapos.core.repository.aop.Transactional)")
    public void annotated() {
    }

    @Around("annotated()")
    public Object inTransaction(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Transactional annotation = signature.getMethod().getAnnotation(Transactional.class);
        if (!annotation.startNew() && context.inTx()) {
            log.trace("Proceed in transaction for {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            return joinPoint.proceed();
        }
        if (annotation.requiredExisting()) {
            throw new IllegalStateException("Transaction was not opened. Required tx before calling " + signature.getDeclaringTypeName() + "." + signature.getName());
        }
        if (annotation.readonly()) {
            return context.getStore().computeInReadonlyTransaction((txn) -> {
                try {
                    log.debug("Evaluate in readonly transaction for {}.{}", signature.getDeclaringTypeName(), signature.getName());
                    Object result = joinPoint.proceed();
                    log.debug("Finish readonly transaction for {}.{}", signature.getDeclaringTypeName(), signature.getName());
                    return result;
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        } else {
            return context.getStore().computeInTransaction((txn) -> {
                try {
                    log.debug("Evaluate in write transaction for {}.{}", signature.getDeclaringTypeName(), signature.getName());
                    Object result = joinPoint.proceed();
                    log.debug("Finish write transaction for {}.{}", signature.getDeclaringTypeName(), signature.getName());
                    return result;
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
    }
}
