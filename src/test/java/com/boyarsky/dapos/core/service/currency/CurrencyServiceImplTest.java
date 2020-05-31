package com.boyarsky.dapos.core.service.currency;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {
    @Mock
    AccountService accountService;
    @Mock
    LedgerService ledgerService;
    @Mock
    CurrencyRepository currencyRepository;
    @Mock
    CurrencyHolderRepository currencyHolderRepository;

    CurrencyService service;
    AccountId sender = CryptoUtils.generateBitcoinWallet().getAccount();
    AccountId recipient = CryptoUtils.generateEd25Wallet().getAccount();

    @BeforeEach
    void setUp() {
        service = new CurrencyServiceImpl(currencyRepository, currencyHolderRepository, accountService, ledgerService);
    }

    @Test
    void add() {
        Transaction tx = mock(Transaction.class);
        CurrencyIssuanceAttachment attachment = new CurrencyIssuanceAttachment((byte) 1, "CODE", "Some named currency", "Useless unit test currency description", 1000, (byte) 2);
        doReturn(sender).when(tx).getSender();
        doReturn(10000L).when(tx).getAmount();
        doReturn(TxType.CURRENCY_ISSUANCE).when(tx).getType();
        doReturn(222L).when(tx).getTxId();
        doReturn(33L).when(tx).getHeight();

        service.add(tx, attachment);

        verify(currencyRepository).save(new Currency(33, 222, "CODE", "Some named currency", "Useless unit test currency description", sender, 1000, 10000, (byte) 2));
        verify(currencyHolderRepository).save(new CurrencyHolder(33, sender, 222, 1000));
        verify(accountService).addToBalance(sender, null, new Operation(222, 33, "CURRENCY_ISSUANCE", -10000));
    }

    @Test
    void transfer_burn_currency() {
        Transaction tx = mock(Transaction.class);
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, 222);
        doReturn(sender).when(tx).getSender();
        doReturn(10000L).when(tx).getAmount();
//        doReturn(TxType.CURRENCY_TRANSFER).when(tx).getType();
        doReturn(90L).when(tx).getTxId();
        doReturn(33L).when(tx).getHeight();
        doReturn(new CurrencyHolder(33, sender, 222, 11000)).when(currencyHolderRepository).get(sender, 222);

        service.transfer(tx, attachment);

        verify(currencyHolderRepository).save(new CurrencyHolder(33, sender, 222, 1000));
        verify(ledgerService).add(new LedgerRecord(90, -10000, "CURRENCY_BURN", sender, null, 33));
    }


    @Test
    void transfer_toAccount() {
        Transaction tx = mock(Transaction.class);
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, 333);
        doReturn(sender).when(tx).getSender();
        doReturn(89L).when(tx).getAmount();
        doReturn(TxType.CURRENCY_TRANSFER).when(tx).getType();
        doReturn(recipient).when(tx).getRecipient();
        doReturn(2L).when(tx).getTxId();
        doReturn(9L).when(tx).getHeight();
        doReturn(new CurrencyHolder(2, sender, 333, 90)).when(currencyHolderRepository).get(sender, 333);

        service.transfer(tx, attachment);

        verify(currencyHolderRepository).save(new CurrencyHolder(9, sender, 333, 1));
        verify(currencyHolderRepository).save(new CurrencyHolder(9, recipient, 333, 89));
        verify(ledgerService).add(new LedgerRecord(2, -89, "CURRENCY_TRANSFER", sender, recipient, 9));

        doReturn(new CurrencyHolder(1, recipient, 333, 10)).when(currencyHolderRepository).get(recipient, 333);
        doReturn(new CurrencyHolder(1, sender, 333, 100)).when(currencyHolderRepository).get(sender, 333);
        doReturn(91L).when(tx).getAmount();

        service.transfer(tx, attachment);

        verify(currencyHolderRepository).save(new CurrencyHolder(9, sender, 333, 9));
        verify(currencyHolderRepository).save(new CurrencyHolder(9, recipient, 333, 101));
        verify(ledgerService).add(new LedgerRecord(2, -91, "CURRENCY_TRANSFER", sender, recipient, 9));
    }

    @Test
    void multiTransfer() {
        Transaction tx = mock(Transaction.class);
        AccountId recipient1 = CryptoUtils.generateBitcoinWallet().getAccount();
        AccountId recipient2 = CryptoUtils.generateEthWallet().getAccount();
        CurrencyMultiAccountAttachment attachment = new CurrencyMultiAccountAttachment((byte) 1, Map.of(
                recipient1, 33L,
                recipient2, 330L
        ), 12L);
        doReturn(sender).when(tx).getSender();
        doReturn(TxType.MULTI_CURRENCY_TRANSFER).when(tx).getType();
        doReturn(-2L).when(tx).getTxId();
        doReturn(10L).when(tx).getHeight();
        doReturn(new CurrencyHolder(1, sender, 12L, 400)).when(currencyHolderRepository).get(sender, 12L);
        doReturn(new CurrencyHolder(2, recipient1, 12L, 10)).when(currencyHolderRepository).get(recipient1, 12L);
        doReturn(null).when(currencyHolderRepository).get(recipient2, 12L);

        service.multiTransfer(tx, attachment);

        verify(currencyHolderRepository).save(new CurrencyHolder(10, recipient2, 12L, 330));
        verify(currencyHolderRepository).save(new CurrencyHolder(10, recipient1, 12L, 43));
        verify(currencyHolderRepository).save(new CurrencyHolder(10, sender, 12L, 37));
        verify(ledgerService).add(new LedgerRecord(-2, -33, "MULTI_CURRENCY_TRANSFER", sender, recipient1, 10));
        verify(ledgerService).add(new LedgerRecord(-2, -330, "MULTI_CURRENCY_TRANSFER", sender, recipient2, 10));

    }


    @Test
    void holders() {
        List<CurrencyHolder> holders = List.of(new CurrencyHolder(1, sender, 1, 399), new CurrencyHolder(2, recipient, 1, 500));
        doReturn(holders).when(currencyHolderRepository).getAllForCurrency(1, new Pagination());

        List<CurrencyHolder> currencyHolders = service.holders(1, new Pagination());

        assertEquals(holders, currencyHolders);
    }

    @Test
    void getAllCurrencies() {
        List<Currency> expected = List.of(mock(Currency.class), mock(Currency.class));
        Pagination pagination = new Pagination();
        doReturn(expected).when(currencyRepository).getAll(pagination);

        List<Currency> allCurrencies = service.getAllCurrencies(pagination);

        assertEquals(expected, allCurrencies);

    }

    @Test
    void getById() {
        Currency expected = mock(Currency.class);
        doReturn(expected).when(currencyRepository).get(2);

        Currency currency = service.getById(2);

        assertEquals(expected, currency);
    }

    @Test
    void accountCurrencies() {
        List<CurrencyHolder> expected = List.of(new CurrencyHolder(1, sender, 2, 900), new CurrencyHolder(2, sender, 3, 1000));
        doReturn(expected).when(currencyHolderRepository).getAllByAccount(sender, new Pagination());

        List<CurrencyHolder> currencyHolders = service.accountCurrencies(sender, new Pagination());
        assertEquals(expected, currencyHolders);
    }

    @Test
    void getCurrencyHolder() {
        CurrencyHolder curHolder = new CurrencyHolder(2, recipient, 1, 1000);
        doReturn(curHolder).when(currencyHolderRepository).get(recipient, 2);

        CurrencyHolder holder = service.getCurrencyHolder(recipient, 2);

        assertEquals(curHolder, holder);
    }

    @Test
    void testClaimReserve() {
        Transaction tx = mock(Transaction.class);
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, -2);
        doReturn(sender).when(tx).getSender();
        doReturn(222L).when(tx).getAmount();
        doReturn(-11L).when(tx).getTxId();
        doReturn(10L).when(tx).getHeight();
        doReturn(new CurrencyHolder(2, sender, -2, 222)).when(currencyHolderRepository).get(sender, -2);
        Currency cur = new Currency(1, -2, "", "", "", recipient, 567, 120, (byte) 2);
        doReturn(cur).when(currencyRepository).get(-2);

        service.claimReserve(tx, attachment);

        verify(currencyHolderRepository).remove(new CurrencyHolder(10, sender, -2, 0));
        verify(currencyRepository).save(cur);
        assertEquals(74, cur.getReserve());
        assertEquals(345, cur.getSupply());
        verify(ledgerService).add(new LedgerRecord(-11, -222L, "CLAIM_CURRENCY_RESERVE", sender, recipient, 10));
        verify(accountService).addToBalance(sender, recipient, new Operation(-11, 10, "CURRENCY_RESERVE_RETURN", 46));
    }


    @Test
    void testClaimReserve_partial() {
        Transaction tx = mock(Transaction.class);
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, -2);
        doReturn(sender).when(tx).getSender();
        doReturn(222L).when(tx).getAmount();
        doReturn(-11L).when(tx).getTxId();
        doReturn(10L).when(tx).getHeight();
        doReturn(new CurrencyHolder(2, sender, -2, 223)).when(currencyHolderRepository).get(sender, -2);
        Currency cur = new Currency(1, -2, "", "", "", recipient, 567, 120, (byte) 2);
        doReturn(cur).when(currencyRepository).get(-2);

        service.claimReserve(tx, attachment);

        verify(currencyHolderRepository).save(new CurrencyHolder(10, sender, -2, 1));
        verify(currencyRepository).save(cur);
        assertEquals(74, cur.getReserve());
        assertEquals(345, cur.getSupply());
        verify(ledgerService).add(new LedgerRecord(-11, -222L, "CLAIM_CURRENCY_RESERVE", sender, recipient, 10));
        verify(accountService).addToBalance(sender, recipient, new Operation(-11, 10, "CURRENCY_RESERVE_RETURN", 46));
    }

    @Test
    void liquidate() {
        Transaction tx = mock(Transaction.class);
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, -2);
        doReturn(sender).when(tx).getSender();
        doReturn(-11L).when(tx).getTxId();
        doReturn(567L).when(tx).getAmount();
        doReturn(10L).when(tx).getHeight();
        doReturn(new CurrencyHolder(2, sender, -2, 567)).when(currencyHolderRepository).get(sender, -2);
        Currency cur = new Currency(1, -2, "", "", "", recipient, 567, 120, (byte) 2);
        doReturn(cur).when(currencyRepository).get(-2);

        service.claimReserve(tx, attachment);

        verify(currencyHolderRepository).remove(new CurrencyHolder(10, sender, -2, 0));
        verify(currencyRepository).remove(cur);
        assertEquals(0, cur.getReserve());
        assertEquals(0, cur.getSupply());
        verify(ledgerService).add(new LedgerRecord(-11, -567L, "CURRENCY_LIQUIDATE", sender, recipient, 10));
        verify(accountService).addToBalance(sender, recipient, new Operation(-11, 10, "CURRENCY_LIQUIDATION_RESERVE_RETURN", 120));
    }
}