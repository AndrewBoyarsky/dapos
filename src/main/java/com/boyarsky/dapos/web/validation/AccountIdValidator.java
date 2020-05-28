package com.boyarsky.dapos.web.validation;

import com.boyarsky.dapos.core.model.account.AccountId;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class AccountIdValidator implements ConstraintValidator<ValidAccount, AccountId> {

    @Override
    public boolean isValid(AccountId value, ConstraintValidatorContext context) {
        return value == null || value.isBitcoin() || value.isEd25() || value.isEth() || value.isVal();
    }
}
