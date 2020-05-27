package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.repository.feeprov.AccountFeeRepository;
import com.boyarsky.dapos.core.repository.feeprov.FeeProviderRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeeProviderServiceImplTest {
    @Mock
    FeeProviderRepository repository;
    @Mock
    AccountFeeRepository accountFeeRepository;
    @Mock
    LedgerService ledgerService;
    @Mock
    AccountService accountService;

    FeeProviderService service;
    AccountId acc1 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId acc2 = TestUtil.generateEd25Acc().getCryptoId();

    @BeforeEach
    void setUp() {
        service = new FeeProviderServiceImpl(accountFeeRepository, repository, ledgerService, accountService);
    }

    @Test
    void handle() {
        Transaction tx = mock(Transaction.class);
        FeeProviderAttachment attachment = new FeeProviderAttachment((byte) 1, State.ACTIVE, new PartyFeeConfig(true, null, null), new PartyFeeConfig(true, null, null));
        doReturn(acc1).when(tx).getSender();
        doReturn(100L).when(tx).getHeight();
        doReturn(1001L).when(tx).getTxId();
        doReturn(555L).when(tx).getAmount();
        doReturn(TxType.SET_FEE_PROVIDER).when(tx).getType();

        service.handle(attachment, tx);

        FeeProvider expected = new FeeProvider(1001, acc1, 555, State.ACTIVE, attachment.getFromFeeConfig(), attachment.getToFeeConfig());
        expected.setHeight(100);
        verify(repository).save(expected);
        verify(accountService).addToBalance(acc1, new Operation(1001, 100, "SET_FEE_PROVIDER", -555));
    }

    @Test
    void charge() {
        FeeProvider feeProvider = new FeeProvider(22, acc1, 900, State.ACTIVE, new PartyFeeConfig(true, null, null), new PartyFeeConfig(true, null, null));
        doReturn(feeProvider).when(repository).get(22);

        service.charge(22L, 550, 100, acc1, null, 90);

        verify(repository).save(feeProvider);
        verify(accountFeeRepository, never()).save(any(AccountFeeAllowance.class));
        assertEquals(100, feeProvider.getHeight());
        assertEquals(350, feeProvider.getBalance());
    }

    @Test
    void charge_with_root_config() {
        FeeProvider feeProvider = new FeeProvider(323, acc1, 15000, State.ACTIVE,
                new PartyFeeConfig(true, new FeeConfig(1000, 20, 2222, Set.of(TxType.PAYMENT)), null),
                new PartyFeeConfig(true, new FeeConfig(1200, 22, 3333, Set.of(TxType.PAYMENT)), null));
        doReturn(feeProvider).when(repository).get(323);
        doReturn(new AccountFeeAllowance(acc1, 323, 2, 1000)).when(accountFeeRepository).getBy(323, acc1);

        service.charge(323L, 933, 4000, acc1, acc2, 90);

        verify(repository).save(feeProvider);
        verify(accountFeeRepository).save(new AccountFeeAllowance(4000, acc1, 323, 1, 67));
        verify(accountFeeRepository).save(new AccountFeeAllowance(4000, acc2, 323, 21, 2400));
        assertEquals(4000, feeProvider.getHeight());
        assertEquals(14067, feeProvider.getBalance());
    }

    @Test
    void allowance() {
    }

    @Test
    void get() {
    }
}