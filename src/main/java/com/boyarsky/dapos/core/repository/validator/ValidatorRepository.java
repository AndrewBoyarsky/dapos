package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;

public interface ValidatorRepository {
    void save(ValidatorEntity validator);

    List<ValidatorEntity> getAll();

    List<ValidatorEntity> getAll(long height);

    List<ValidatorEntity> getAll(Boolean enabled, Pagination pagination);


//    List<ValidatorEntity> getAllWith(long absentFor, boolean enabled);

    ValidatorEntity getById(AccountId id);
}
