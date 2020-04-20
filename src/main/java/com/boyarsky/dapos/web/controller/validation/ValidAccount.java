package com.boyarsky.dapos.web.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Constraint(validatedBy = AccountIdValidator.class)
public @interface ValidAccount {
    String message() default "Invalid account";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
