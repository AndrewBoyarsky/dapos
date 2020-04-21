package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;

import java.util.List;

public interface AccountFeeRepository {
    AccountFeeAllowance getBy(long feeProvId, AccountId accountId);

    List<AccountFeeAllowance> getAll();

    void save(AccountFeeAllowance fee);
}
