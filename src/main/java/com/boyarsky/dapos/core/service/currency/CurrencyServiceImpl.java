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
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        accountService.addToBalance(tx.getSender(), null, new Operation(tx.getTxId(), tx.getHeight(), tx.getType().toString(), -tx.getAmount()));
    }

    @Override
    public void transfer(Transaction tx, CurrencyIdAttachment attachment) {
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

    public void claimReserve(Currency currency, Transaction tx) {
        CurrencyHolder holder = currencyHolderRepository.get(tx.getSender(), currency.getCurrencyId());
        BigDecimal sendersPercent = BigDecimal.valueOf(tx.getAmount()).divide(BigDecimal.valueOf(currency.getSupply()), 4, RoundingMode.DOWN);
        long claimedReserve = sendersPercent.multiply(BigDecimal.valueOf(currency.getReserve())).toBigInteger().longValueExact();
        ledgerService.add(new LedgerRecord(tx.getTxId(), -tx.getAmount(), "CLAIM_CURRENCY_RESERVE", tx.getSender(), currency.getIssuer(), tx.getHeight()));
        accountService.addToBalance(tx.getSender(), currency.getIssuer(), new Operation(tx.getTxId(), tx.getHeight(), "RESERVE_RETURN", claimedReserve));
        holder.setAmount(holder.getAmount() - tx.getAmount());
        holder.setHeight(tx.getHeight());
        save(holder);
        currency.setHeight(tx.getHeight());
        currency.setSupply(currency.getSupply() - tx.getAmount());
        currency.setReserve(currency.getReserve() - claimedReserve);
        currencyRepository.save(currency);
    }

    @Override
    public void claimReserve(Transaction tx, CurrencyIdAttachment idAttachment) {
        Currency currency = getById(idAttachment.getCurrencyId());
        if (tx.getAmount() == currency.getSupply()) {
            liquidate(currency, tx);
        } else {
            claimReserve(currency, tx);
        }
    }

    public void liquidate(Currency currency, Transaction tx) {
        AccountId sender = tx.getSender();
        CurrencyHolder holder = currencyHolderRepository.get(sender, currency.getCurrencyId());
        holder.setHeight(tx.getHeight());
        holder.setAmount(0);
        save(holder);
        accountService.addToBalance(sender, currency.getIssuer(), new Operation(tx.getTxId(), tx.getHeight(), "LIQUIDATION_RESERVE_RETURN", currency.getReserve()));
        ledgerService.add(new LedgerRecord(tx.getTxId(), -currency.getSupply(), "CURRENCY_LIQUIDATE", sender, currency.getIssuer(), tx.getHeight()));
        currency.setSupply(0);
        currency.setHeight(tx.getHeight());
        currency.setReserve(0);
        currencyRepository.remove(currency);
    }

    private void save(CurrencyHolder holder) {
        if (holder.getAmount() == 0) {
            currencyHolderRepository.remove(holder);
        } else {
            currencyHolderRepository.save(holder);
        }
    }
}
