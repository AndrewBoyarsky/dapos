package com.boyarsky.dapos.core.repository.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Transactional {
    boolean readonly() default false;

    boolean startNew() default false;

    boolean requiredExisting() default false; // when true -> require existing blockchain transactions, otherwise -> will open new
}
