package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;

public interface FeeProviderService {
    void handle(FeeProviderAttachment attachment, Transaction tx);

    void charge(long id, long amount, AccountId sender, AccountId recipient);

    AccountFeeAllowance allowance(long id, AccountId accountId);

    FeeProvider get(long id);
}
