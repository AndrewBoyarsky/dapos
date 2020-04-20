package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;

public interface AccountFeeRepository {
    AccountFeeAllowance getBy(long feeProvId, AccountId accountId);

    void save(AccountFeeAllowance fee);
}
