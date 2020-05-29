package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;

import java.util.List;

public interface FeeProviderService {
    void handle(FeeProviderAttachment attachment, Transaction tx);

    void charge(long id, long amount, long height, AccountId sender, AccountId recipient, long eventId);

    List<FeeProvider> availableForAccount(AccountId id);

    List<FeeProvider> availableForTx(AccountId id, AccountId recipient, TxType type, long fee);

    AccountFeeAllowance allowance(long id, AccountId accountId, boolean sender);

    FeeProvider get(long id);

    List<FeeProvider> getAll();
}
