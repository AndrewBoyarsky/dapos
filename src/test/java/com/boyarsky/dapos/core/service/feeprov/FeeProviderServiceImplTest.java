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

import java.util.List;
import java.util.Map;
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
    AccountId acc3 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId acc4 = TestUtil.generateEd25Acc().getCryptoId();

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
        verify(accountService).addToBalance(acc1, null, new Operation(1001, 100, "SET_FEE_PROVIDER", -555));
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
        doReturn(new AccountFeeAllowance(acc1, 323, true, 2, 1000)).when(accountFeeRepository).getBy(323, acc1, true);

        service.charge(323L, 933, 4000, acc1, acc2, 90);

        verify(repository).save(feeProvider);
        verify(accountFeeRepository).save(new AccountFeeAllowance(4000, acc1, 323, true, 1, 67));
        verify(accountFeeRepository).save(new AccountFeeAllowance(4000, acc2, 323, false, 21, 2400));
        assertEquals(4000, feeProvider.getHeight());
        assertEquals(14067, feeProvider.getBalance());
    }

    @Test
    void availableForAcc() {
        FeeProvider feeProv1 = new FeeProvider(323, acc1, 15000, State.ACTIVE,
                new PartyFeeConfig(true, null, null),
                new PartyFeeConfig(true, null, null));
        FeeProvider feeProv2 = new FeeProvider(324, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1, 2, 3, Set.of()), List.of(acc2, acc1),
                        new FeeConfig(90, 2, 3, Set.of()), List.of(acc3, acc4))),
                new PartyFeeConfig(true, null, null));
        FeeProvider feeProv3 = new FeeProvider(325, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1, 2, 3, Set.of()), List.of(acc2, acc1, acc4))),
                new PartyFeeConfig(true, null, null));

        FeeProvider feeProv4 = new FeeProvider(326, acc1, 9999, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(mock(FeeConfig.class), List.of(acc2), mock(FeeConfig.class), List.of(acc1), mock(FeeConfig.class), List.of(acc4),
                        new FeeConfig(1, 2, 3, Set.of()), List.of(acc3))),
                new PartyFeeConfig(true, null, null));

        doReturn(List.of(feeProv1, feeProv2, feeProv3, feeProv4)).when(repository).getAll(State.ACTIVE);
        doReturn(null).when(accountFeeRepository).getBy(324, acc3, true);
        doReturn(new AccountFeeAllowance(22, acc3, 323, true, 0, 0)).when(accountFeeRepository).getBy(323, acc3, true);
        doReturn(new AccountFeeAllowance(25, acc3, 326, true, 2, 3)).when(accountFeeRepository).getBy(326, acc3, true);

        List<FeeProvider> feeProviders = service.availableForAccount(acc3);

        assertEquals(List.of(feeProv2, feeProv4), feeProviders);
    }

    @Test
    void availableForTx() {
        FeeProvider feeProv1 = new FeeProvider(323, acc1, 15000, State.ACTIVE,
                new PartyFeeConfig(true, null, null),
                new PartyFeeConfig(true, new FeeConfig(1200, -1, -1, Set.of()), null));

        FeeProvider feeProv11 = new FeeProvider(3231, acc1, 3333, State.ACTIVE,
                new PartyFeeConfig(true, null, null),
                new PartyFeeConfig(true, null, null));
        FeeProvider feeProv2 = new FeeProvider(324, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1499, 2, 3, Set.of()), List.of(acc2, acc1),
                        new FeeConfig(90000, 2, 900000, Set.of()), List.of(acc3, acc4))),
                new PartyFeeConfig(true, null, null));
        FeeProvider feeProv21 = new FeeProvider(3241, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1500, 2, 2222, Set.of(TxType.MESSAGE)), List.of(acc2, acc1),
                        new FeeConfig(90000, 2, 900000, Set.of()), List.of(acc3, acc4))),
                new PartyFeeConfig(true, null, null));
        FeeProvider feeProv22 = new FeeProvider(3242, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1500, 2, 2222, Set.of(TxType.PAYMENT, TxType.VOTE)), List.of(acc2, acc1),
                        new FeeConfig(90000, 2, 900000, Set.of()), List.of(acc3, acc4))),
                new PartyFeeConfig(true, null, null));

        FeeProvider feeProv3 = new FeeProvider(325, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(9000, 2, 100000, Set.of()), List.of(acc2, acc1, acc4))),
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1500, 3, 9999, Set.of()), List.of(acc2, acc3), new FeeConfig(9000, 3, 33333, Set.of()), List.of(acc1, acc4))));

        FeeProvider feeProv31 = new FeeProvider(3251, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(9000, 2, 100000, Set.of()), List.of(acc2, acc1, acc4))),
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1500, 3, 9999, Set.of()), List.of(acc2, acc3), new FeeConfig(9000, 3, 33333, Set.of()), List.of(acc1, acc4))));

        FeeProvider feeProv32 = new FeeProvider(3252, acc1, 3000, State.ACTIVE,
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(9000, 2, 100000, Set.of()), List.of(acc2, acc1, acc4))),
                new PartyFeeConfig(false, null, Map.of(new FeeConfig(1500, 3, 9999, Set.of()), List.of(acc2, acc3), new FeeConfig(9000, 3, 33333, Set.of()), List.of(acc1, acc4))));


        doReturn(List.of(feeProv1, feeProv11, feeProv2, feeProv21, feeProv22, feeProv3, feeProv31, feeProv32)).when(repository).getAll(State.ACTIVE, 1500);
        doReturn(null).when(accountFeeRepository).getBy(325, acc3, false);
        doReturn(null).when(accountFeeRepository).getBy(325, acc2, true);
        doReturn(new AccountFeeAllowance(34, acc2, 3242, true, 2, 1500)).when(accountFeeRepository).getBy(3242, acc2, true);
        doReturn(new AccountFeeAllowance(22, acc3, 3252, false, 0, 0)).when(accountFeeRepository).getBy(3252L, acc3, false);
        doReturn(null).when(accountFeeRepository).getBy(3252L, acc2, true);
        doReturn(new AccountFeeAllowance(25, acc2, 3251, true, 2, 1499)).when(accountFeeRepository).getBy(3251L, acc2, true);

        List<FeeProvider> feeProviders = service.availableForTx(acc2, acc3, TxType.PAYMENT, 1500);

        assertEquals(List.of(feeProv11, feeProv22, feeProv3), feeProviders);

        List<FeeProvider> feeProvsNoRecipient = service.availableForTx(acc2, null, TxType.MESSAGE, 1500);

        assertEquals(List.of(feeProv1, feeProv11, feeProv21, feeProv3, feeProv32), feeProvsNoRecipient);
    }
}