package com.boyarsky.dapos.core.service.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.currency.CurrencyHolderRepository;
import com.boyarsky.dapos.core.repository.currency.CurrencyRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyTransferAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    private CurrencyRepository currencyRepository;
    private CurrencyHolderRepository currencyHolderRepository;
    private AccountService accountService;
    private LedgerService ledgerService;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository, CurrencyHolderRepository currencyHolderRepository, AccountService accountService, LedgerService ledgerService) {
        this.currencyRepository = currencyRepository;
        this.currencyHolderRepository = currencyHolderRepository;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    @Override
    public void add(Transaction tx, CurrencyIssuanceAttachment attachment) {
        Currency newCurrency = new Currency(tx.getHeight(), tx.getTxId(), attachment.getCode(), attachment.getName(), attachment.getDescription(), tx.getSender(), attachment.getSupply(), tx.getAmount(), attachment.getDecimals());
        currencyRepository.save(newCurrency);
        CurrencyHolder account = new CurrencyHolder(tx.getHeight(), tx.getSender(), tx.getTxId(), attachment.getSupply());
        currencyHolderRepository.save(account);
        accountService.addToBalance(tx.getSender(), new Operation(tx.getTxId(), tx.getHeight(), tx.getType().toString(), -tx.getAmount()));
    }

    @Override
    public void transfer(Transaction tx, CurrencyTransferAttachment attachment) {
        CurrencyHolder holder = currencyHolderRepository.get(tx.getSender(), attachment.getCurrencyId());
        holder.setAmount(holder.getAmount() - tx.getAmount());
        holder.setHeight(tx.getHeight());
        currencyHolderRepository.save(holder);
        if (tx.getRecipient() != null) {
            CurrencyHolder recipient = currencyHolderRepository.get(tx.getRecipient(), attachment.getCurrencyId());
            if (recipient == null) {
                recipient = new CurrencyHolder(tx.getHeight(), tx.getRecipient(), attachment.getCurrencyId(), tx.getAmount());
            } else {
                recipient.setHeight(tx.getHeight());
                recipient.setAmount(recipient.getAmount() + tx.getAmount());
            }
            currencyHolderRepository.save(recipient);
            ledgerService.add(new LedgerRecord(tx.getTxId(), -tx.getAmount(), tx.getType().toString(), tx.getSender(), tx.getRecipient(), tx.getHeight()));
        } else {
            ledgerService.add(new LedgerRecord(tx.getTxId(), -tx.getAmount(), "CURRENCY_BURN", tx.getSender(), null, tx.getHeight()));
        }
    }

    @Override
    public List<CurrencyHolder> holders(long currencyId) {
        return currencyHolderRepository.getAllForCurrency(currencyId);
    }

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.getAll();
    }

    @Override
    public Currency getById(long currencyId) {
        return currencyRepository.get(currencyId);
    }

    @Override
    public List<CurrencyHolder> accountCurrencies(AccountId accountId) {
        return currencyHolderRepository.getAllByAccount(accountId);
    }

    @Override
    public CurrencyHolder getCurrencyHolder(AccountId accountId, long currencyId) {
        return currencyHolderRepository.get(accountId, currencyId);
    }

    @Override
    public boolean reserved(String code) {
        return currencyRepository.getByCode(code) != null;
    }

}
