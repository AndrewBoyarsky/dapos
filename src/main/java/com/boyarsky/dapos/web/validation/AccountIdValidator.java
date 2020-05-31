package com.boyarsky.dapos.web.validation;

import com.boyarsky.dapos.core.model.account.AccountId;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class AccountIdValidator implements ConstraintValidator<ValidAccount, AccountId> {
    ValidAccount constraint;

    @Override
    public void initialize(ValidAccount constraintAnnotation) {
        constraint = constraintAnnotation;
    }

    @Override
    public boolean isValid(AccountId value, ConstraintValidatorContext context) {
        return value == null || value.isBitcoin() && contains("btc") || value.isEd25() && contains("ed25") || value.isEth() && contains("eth") || value.isVal() && contains("validator");
    }

    private boolean contains(String s) {
        for (String type : constraint.allowedTypes()) {
            if (type.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
}
