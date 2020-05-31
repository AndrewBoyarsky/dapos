package com.boyarsky.dapos.web.validation;

import com.boyarsky.dapos.utils.Convert;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class HexadecimalByteArrayValidator implements ConstraintValidator<ValidHexadecimalArray, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value != null) {
            try {
                Convert.parseHexString(value);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
