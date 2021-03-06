package com.boyarsky.dapos.web.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Constraint(validatedBy = AccountIdValidator.class)
public @interface ValidAccount {
    String message() default "Invalid account";

    String[] allowedTypes() default {"VALIDATOR", "ETH", "BTC", "ED25"};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
