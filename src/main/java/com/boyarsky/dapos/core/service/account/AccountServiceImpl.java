package com.boyarsky.dapos.core.service.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.account.AccountRepository;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private AccountRepository repository;
    private LedgerService ledgerService;

    @Autowired
    public AccountServiceImpl(AccountRepository repository, LedgerService ledgerService) {
        this.repository = repository;
        this.ledgerService = ledgerService;
    }

    @Override
    public Account get(AccountId accountId) {
        Account account = repository.find(accountId);
        if (account == null) {
            throw new NotFoundException("Account with id " + accountId.toString() + " does not exist");
        }
        return account;
    }

    @Override
    public boolean assignPublicKey(AccountId accountId, byte[] publicKey) {
        Account account = get(accountId);
        if (account.getPublicKey() != null) {
            return false;
        }
        account.setPublicKey(publicKey);
        save(account);
        return true;
    }

    @Override
    public List<Account> getAll() {
        return repository.getAll();
    }

    @Override
    public void transferMoney(AccountId sender, AccountId recipient, Operation op) {
        if (op.getAmount() == 0) {
            return;
        }
        if (recipient != null) {
            addToBalance(recipient, op);
        }
        op.setAmount(-op.getAmount());
        addToBalance(sender, op);
        ledgerService.add(new LedgerRecord(op.getId(), op.getAmount(), op.getType(), sender, recipient, op.getHeight()));
    }

    @Override
    public void addToBalance(AccountId accountId, Operation op) {
        if (op.getAmount() == 0) {
            return;
        }
        Account account = repository.find(accountId);
        if (account == null) {
            account = new Account(accountId, null, 0, Account.Type.ORDINARY);
        }
        account.setHeight(op.getHeight());
        account.setBalance(account.getBalance() + op.getAmount());
        save(account);
    }

    /**
     * Do not charge sender, will deposit balance for the given account and track ledger
     *
     * @param acc      account whose balance should be deposited
     * @param senderId account which is assumed to send these funds but it will not be verified
     * @param op       operation under which balance should be credited
     */
    @Override
    public void addToBalance(AccountId acc, AccountId senderId, Operation op) {
        if (op.getAmount() == 0) {
            return;
        }
        addToBalance(acc, op);
        ledgerService.add(new LedgerRecord(op.getId(), op.getAmount(), op.getType(), senderId, acc, op.getHeight()));
    }

    @Override
    public void save(Account account) {
        repository.save(account);
    }
}
