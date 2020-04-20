package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.FeeProvider;
import com.boyarsky.dapos.core.model.account.AccountId;

import java.util.List;

public interface FeeProviderRepository {
    void save(FeeProvider feeProvider);

    FeeProvider get(long id);

    List<FeeProvider> getByAccount(AccountId id);
}
