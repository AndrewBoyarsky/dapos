package com.boyarsky.dapos.web.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Constraint(validatedBy = HexadecimalByteArrayValidator.class)
public @interface ValidHexadecimalArray {
    String message() default "Invalid hexadecimal byte array";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
