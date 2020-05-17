package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ValidatorAttachment;

import java.util.List;

public interface ValidatorService {
    void controlValidator(Transaction tx, ValidatorAttachment attachment);

    List<ValidatorEntity> updatedAt(long height);

    List<ValidatorEntity> getAbsentNodes();

    void absentValidator(AccountId validatorId);

    long punishByzantine(AccountId validatorId);

    long punishAbsent(AccountId validatorId);

    void distributeReward(List<ValidatorEntity> fairValidators, long rewardAmount);

    ValidatorEntity get(AccountId fairValidator);
}

