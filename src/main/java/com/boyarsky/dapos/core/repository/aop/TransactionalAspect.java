package com.boyarsky.dapos.core.repository.aop;

import com.boyarsky.dapos.core.repository.XodusRepoContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
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
        if (context.inTx()) {
            return joinPoint.proceed();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Transactional annotation = signature.getMethod().getAnnotation(Transactional.class);
        if (annotation.requiredExisting()) {
            throw new IllegalStateException("Transaction was not opened. Required tx before calling " + signature.getDeclaringTypeName() + "." + signature.getName());
        }
        if (annotation.readonly()) {
            return context.getStore().computeInReadonlyTransaction((txn) -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        } else {
            return context.getStore().computeInTransaction((txn) -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
    }
}
