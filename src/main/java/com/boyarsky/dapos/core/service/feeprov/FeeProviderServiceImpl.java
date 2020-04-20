package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.repository.feeprov.AccountFeeRepository;
import com.boyarsky.dapos.core.repository.feeprov.FeeProviderRepository;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FeeProviderServiceImpl implements FeeProviderService {
    FeeProviderRepository repository;
    Blockchain blockchain;
    final AccountFeeRepository accountFeeRepository;

    @Autowired
    public FeeProviderServiceImpl(FeeProviderRepository repository, Blockchain blockchain, AccountFeeRepository accountFeeRepository) {
        this.repository = repository;
        this.blockchain = blockchain;
        this.accountFeeRepository = accountFeeRepository;
    }

    @Override
    public void handle(FeeProviderAttachment attachment, Transaction tx) {
        FeeProvider feeProvider = new FeeProvider(tx.getTxId(), tx.getSender(), tx.getAmount(), attachment.getState(), attachment.getFromFeeConfig(), attachment.getToFeeConfig());
        feeProvider.setHeight(blockchain.getCurrentBlockHeight());
        repository.save(feeProvider);
    }

    @Override
    public void charge(long id, long amount, AccountId sender, AccountId recipient) {
        FeeProvider feeProvider = get(id);
        feeProvider.setBalance(feeProvider.getBalance() - amount);
        feeProvider.setHeight(blockchain.getCurrentBlockHeight());
        repository.save(feeProvider);
        chargeAllowance(feeProvider, sender, amount);
        if (recipient != null) {
            chargeAllowance(feeProvider, recipient, amount);
        }
    }

    private void chargeAllowance(FeeProvider feeProvider, AccountId account, long amount) {
        AccountFeeAllowance allowance = accountFeeRepository.getBy(feeProvider.getId(), account);
        Optional<FeeConfig> feeConfigOpt = feeProvider.getFromFeeConfig().forAccount(account);
        if (allowance == null && feeConfigOpt.isPresent()) {
            FeeConfig feeConfig = feeConfigOpt.get();
            allowance = new AccountFeeAllowance(account, feeProvider.getId(), feeConfig.getMaxAllowedOperations(), feeConfig.getMaxAllowedTotalFee());
        }
        if (allowance != null) {
            allowance.setOperations(allowance.getOperations() - 1);
            allowance.setFeeRemaining(allowance.getFeeRemaining() - amount);
            allowance.setHeight(blockchain.getCurrentBlockHeight());
            accountFeeRepository.save(allowance);
        }
    }

    @Override
    public AccountFeeAllowance allowance(long id, AccountId accountId) {
        return accountFeeRepository.getBy(id, accountId);
    }

    @Override
    public FeeProvider get(long id) {
        return repository.get(id);
    }
}
