package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.repository.feeprov.AccountFeeRepository;
import com.boyarsky.dapos.core.repository.feeprov.FeeProviderRepository;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FeeProviderServiceImpl implements FeeProviderService {
    FeeProviderRepository repository;
    final AccountFeeRepository accountFeeRepository;

    @Autowired
    public FeeProviderServiceImpl(FeeProviderRepository repository, AccountFeeRepository accountFeeRepository) {
        this.repository = repository;
        this.accountFeeRepository = accountFeeRepository;
    }

    @Override
    public void handle(FeeProviderAttachment attachment, Transaction tx) {
        FeeProvider feeProvider = new FeeProvider(tx.getTxId(), tx.getSender(), tx.getAmount(), attachment.getState(), attachment.getFromFeeConfig(), attachment.getToFeeConfig());
        feeProvider.setHeight(tx.getHeight());
        repository.save(feeProvider);
    }

    @Override
    public void charge(long id, long amount, long height, AccountId sender, AccountId recipient) {
        FeeProvider feeProvider = get(id);
        feeProvider.setBalance(feeProvider.getBalance() - amount);
        feeProvider.setHeight(height);
        repository.save(feeProvider);
        chargeAllowance(feeProvider, height, sender, amount);
        if (recipient != null) {
            chargeAllowance(feeProvider, height, recipient, amount);
        }
    }

    private void chargeAllowance(FeeProvider feeProvider, long height, AccountId account, long amount) {
        AccountFeeAllowance allowance = accountFeeRepository.getBy(feeProvider.getId(), account);
        Optional<FeeConfig> feeConfigOpt = feeProvider.getFromFeeConfig().forAccount(account);
        if (allowance == null && feeConfigOpt.isPresent()) {
            FeeConfig feeConfig = feeConfigOpt.get();
            allowance = new AccountFeeAllowance(account, feeProvider.getId(), feeConfig.getMaxAllowedOperations(), feeConfig.getMaxAllowedTotalFee());
        }
        if (allowance != null) {
            allowance.setOperations(allowance.getOperations() - 1);
            allowance.setFeeRemaining(allowance.getFeeRemaining() - amount);
            allowance.setHeight(height);
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
