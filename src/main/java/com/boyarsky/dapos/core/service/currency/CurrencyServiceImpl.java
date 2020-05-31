package com.boyarsky.dapos.core.service.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.currency.CurrencyHolderRepository;
import com.boyarsky.dapos.core.repository.currency.CurrencyRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyMultiAccountAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

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
        doTransfer(tx.getTxId(), tx.getHeight(), tx.getType(), tx.getSender(), tx.getRecipient(), tx.getAmount(), attachment.getCurrencyId());
    }

    private void doTransfer(long eventId, long height, TxType type, AccountId sender, AccountId recipient, long amount, long currencyId) {
        addCurrencyNoCheckNoLog(sender, -amount, height, currencyId);
        if (recipient != null) {
            addCurrencyTo(eventId, height, type, sender, recipient, amount, currencyId);
        } else {
            ledgerService.add(new LedgerRecord(eventId, -amount, LedgerRecord.Type.CURRENCY_BURN.toString(), sender, null, height));
        }
    }

    private void addCurrencyTo(long eventId, long height, TxType type, AccountId sender, AccountId recipient, long amount, long currencyId) {
        CurrencyHolder currencyHolder = currencyHolderRepository.get(recipient, currencyId);
        if (currencyHolder == null) {
            currencyHolder = new CurrencyHolder(height, recipient, currencyId, amount);
        } else {
            currencyHolder.setHeight(height);
            currencyHolder.setAmount(currencyHolder.getAmount() + amount);
        }
        currencyHolderRepository.save(currencyHolder);
        ledgerService.add(new LedgerRecord(eventId, -amount, type.toString(), sender, recipient, height));
    }

    private void addCurrencyNoCheckNoLog(AccountId id, long amount, long height, long currencyId) {
        CurrencyHolder holder = currencyHolderRepository.get(id, currencyId);
        holder.setAmount(holder.getAmount() + amount);
        holder.setHeight(height);
        currencyHolderRepository.save(holder);
    }

    @Override
    public void multiTransfer(Transaction tx, CurrencyMultiAccountAttachment attachment) {
        long currencyId = attachment.getCurrencyId();
        long totalAmount = 0;
        for (Map.Entry<AccountId, Long> entry : attachment.getTransfers().entrySet()) {
            AccountId recipient = entry.getKey();
            Long amount = entry.getValue();
            addCurrencyTo(tx.getTxId(), tx.getHeight(), tx.getType(), tx.getSender(), recipient, amount, currencyId);
            totalAmount += amount;
        }
        addCurrencyNoCheckNoLog(tx.getSender(), -totalAmount, tx.getHeight(), currencyId);
    }

    @Override
    public List<CurrencyHolder> holders(long currencyId, Pagination pagination) {
        return currencyHolderRepository.getAllForCurrency(currencyId, pagination);
    }

    @Override
    public List<Currency> getAllCurrencies(Pagination pagination) {
        return currencyRepository.getAll(pagination);
    }

    @Override
    public Currency getById(long currencyId) {
        return currencyRepository.get(currencyId);
    }

    @Override
    public List<CurrencyHolder> accountCurrencies(AccountId accountId, Pagination pagination) {
        return currencyHolderRepository.getAllByAccount(accountId, pagination);
    }

    @Override
    public CurrencyHolder getCurrencyHolder(AccountId accountId, long currencyId) {
        return currencyHolderRepository.get(accountId, currencyId);
    }

    @Override
    public Currency getByCode(String code) {
        return currencyRepository.getByCode(code);
    }

    @Override
    public boolean reserved(String code) {
        return currencyRepository.getByCode(code) != null;
    }

    public void claimReserve(Currency currency, Transaction tx) {
        CurrencyHolder holder = currencyHolderRepository.get(tx.getSender(), currency.getCurrencyId());
        BigDecimal sendersPercent = BigDecimal.valueOf(tx.getAmount()).divide(BigDecimal.valueOf(currency.getSupply()), 4, RoundingMode.DOWN);
        long claimedReserve = sendersPercent.multiply(BigDecimal.valueOf(currency.getReserve())).toBigInteger().longValueExact();
        ledgerService.add(new LedgerRecord(tx.getTxId(), -tx.getAmount(), LedgerRecord.Type.CLAIM_CURRENCY_RESERVE.toString(), tx.getSender(), currency.getIssuer(), tx.getHeight()));
        accountService.addToBalance(tx.getSender(), currency.getIssuer(), new Operation(tx.getTxId(), tx.getHeight(), LedgerRecord.Type.CURRENCY_RESERVE_RETURN.toString(), claimedReserve));
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
        accountService.addToBalance(sender, currency.getIssuer(), new Operation(tx.getTxId(), tx.getHeight(), LedgerRecord.Type.CURRENCY_LIQUIDATION_RESERVE_RETURN.toString(), currency.getReserve()));
        ledgerService.add(new LedgerRecord(tx.getTxId(), -currency.getSupply(), LedgerRecord.Type.CURRENCY_LIQUIDATE.toString(), sender, currency.getIssuer(), tx.getHeight()));
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
