package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RegisterValidatorAttachment;

import java.util.List;

public interface ValidatorService {
    void registerValidator(Transaction tx, RegisterValidatorAttachment attachment);

    void toggleValidator(AccountId validatorId, boolean enabled, long height);

    void absentValidator(AccountId validatorId);

    long punishByzantine(AccountId validatorId, long height);

    long punishAbsent(AccountId validatorId, long height);

    void distributeReward(List<ValidatorEntity> fairValidators, long rewardAmount, long height);

    ValidatorEntity get(AccountId id);
}

