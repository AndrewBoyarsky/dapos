package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.feeprov.AccountFeeRepository;
import com.boyarsky.dapos.core.repository.feeprov.FeeProviderRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeeProviderServiceImpl implements FeeProviderService {
    private final AccountFeeRepository accountFeeRepository;
    private FeeProviderRepository repository;
    private LedgerService ledgerService;
    private AccountService accountService;

    @Autowired
    public FeeProviderServiceImpl(AccountFeeRepository accountFeeRepository, FeeProviderRepository repository, LedgerService ledgerService, AccountService accountService) {
        this.accountFeeRepository = accountFeeRepository;
        this.repository = repository;
        this.ledgerService = ledgerService;
        this.accountService = accountService;
    }

    @Override
    public void handle(FeeProviderAttachment attachment, Transaction tx) {
        FeeProvider feeProvider = new FeeProvider(tx.getTxId(), tx.getSender(), tx.getAmount(), attachment.getState(), attachment.getFromFeeConfig(), attachment.getToFeeConfig());
        feeProvider.setHeight(tx.getHeight());
        accountService.addToBalance(tx.getSender(), null, new Operation(tx.getTxId(), tx.getHeight(), tx.getType().toString(), -tx.getAmount()));
        repository.save(feeProvider);
    }

    @Override
    public void charge(long id, long amount, long height, AccountId sender, AccountId recipient, long eventId) {
        FeeProvider feeProvider = get(id);
        feeProvider.setBalance(feeProvider.getBalance() - amount);
        feeProvider.setHeight(height);
        ledgerService.add(new LedgerRecord(id, -amount, "Charge Fee Provider", sender, recipient, height));
        repository.save(feeProvider);
        chargeAllowance(id, feeProvider.getFromFeeConfig(), height, sender, amount);
        if (recipient != null) {
            chargeAllowance(id, feeProvider.getToFeeConfig(), height, recipient, amount);
        }
    }

    private void chargeAllowance(long id, PartyFeeConfig config, long height, AccountId account, long amount) {
        AccountFeeAllowance allowance = accountFeeRepository.getBy(id, account);
        Optional<FeeConfig> feeConfigOpt = config.forAccount(account);
        if (allowance == null && feeConfigOpt.isPresent()) {
            FeeConfig feeConfig = feeConfigOpt.get();
            allowance = new AccountFeeAllowance(account, id, feeConfig.getMaxAllowedOperations(), feeConfig.getMaxAllowedTotalFee());
        }
        if (allowance != null) {
            allowance.setOperations(allowance.getOperations() - 1);
            allowance.setFeeRemaining(allowance.getFeeRemaining() - amount);
            allowance.setHeight(height);
            accountFeeRepository.save(allowance);
        }
    }

    @Override
    public List<FeeProvider> availableForAccount(AccountId id) {
        List<FeeProvider> all = repository.getAll(State.ACTIVE);
        List<FeeProvider> needed = new ArrayList<>();
        for (FeeProvider feeProvider : all) {
            if (feeProvider.getFromFeeConfig().isWhitelistAll() || feeProvider.getFromFeeConfig().getConfigs().values().stream().flatMap(List::stream).anyMatch(id::equals)) {
                AccountFeeAllowance allowance = accountFeeRepository.getBy(feeProvider.getId(), id);
                if (allowance == null || allowance.getOperations() > 0) {
                    needed.add(feeProvider);
                }
            }
        }
        return needed;
    }

    @Override
    public AccountFeeAllowance allowance(long id, AccountId accountId) {
        return accountFeeRepository.getBy(id, accountId);
    }

    @Override
    public FeeProvider get(long id) {
        return repository.get(id);
    }

    @Override
    public List<FeeProvider> getAll() {
        return repository.getAll();
    }
}
