package com.boyarsky.dapos.core.service.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.repository.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private AccountRepository repository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository) {
        this.repository = repository;
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
    public void transferMoney(AccountId sender, AccountId recipient, long amount) {
        Account senderAccount = get(sender);
        if (recipient != null) {
            Account recAccount = repository.find(recipient);
            if (recAccount == null) {
                recAccount = new Account(recipient, null, 0, Account.Type.ORDINARY);
            }
            recAccount.setBalance(recAccount.getBalance() + amount);
            save(recAccount);
        }
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        save(senderAccount);
    }

    @Override
    public void save(Account account) {
        repository.save(account);
    }
}
