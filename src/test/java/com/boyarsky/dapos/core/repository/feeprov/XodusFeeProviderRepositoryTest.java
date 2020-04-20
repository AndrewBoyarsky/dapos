package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.AccountUtil;
import com.boyarsky.dapos.core.model.FeeConfig;
import com.boyarsky.dapos.core.model.FeeProvider;
import com.boyarsky.dapos.core.model.PartyFeeConfig;
import com.boyarsky.dapos.core.model.State;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.repository.RepoTest;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@ComponentScan("com.boyarsky.dapos.core.repository.feeprov")
@ContextConfiguration(classes = {RepoTest.Config.class, XodusFeeProviderRepositoryTest.class})
public class XodusFeeProviderRepositoryTest extends RepoTest {
    @Autowired
    FeeProviderRepository repo;
    private AccountId alice = AccountUtil.generateEd25Acc().getCryptoId();
    private AccountId bob = AccountUtil.generateEd25Acc().getCryptoId();
    private AccountId chuck = AccountUtil.generateEd25Acc().getCryptoId();
    private PartyFeeConfig allowAllConfig = new PartyFeeConfig(true, null, null);
    FeeProvider feeProvider1 = new FeeProvider(1L, alice, 100, State.ACTIVE, allowAllConfig, allowAllConfig);
    private PartyFeeConfig generalRestrictiveConfig = new PartyFeeConfig(true, new FeeConfig(10, 2, 20, Set.of()), null);
    FeeProvider feeProvider2 = new FeeProvider(2L, bob, 1000, State.STOPPED, allowAllConfig, generalRestrictiveConfig);
    FeeProvider feeProvider3 = new FeeProvider(3L, alice, 1000, State.SUSPENDED, generalRestrictiveConfig, generalRestrictiveConfig);
    private PartyFeeConfig accountRestrictiveConfig = new PartyFeeConfig(false, null, Map.of(new FeeConfig(-1, 2, 1, Set.of(TxType.PAYMENT)),
            List.of(bob, alice),
            new FeeConfig(1, 1, 1, Set.of()), List.of(chuck)
    ));
    FeeProvider feeProvider4 = new FeeProvider(4L, alice, 500, State.ACTIVE, generalRestrictiveConfig, accountRestrictiveConfig);
    FeeProvider feeProvider5 = new FeeProvider(5L, bob, 5000, State.ACTIVE, accountRestrictiveConfig, allowAllConfig);

    @BeforeEach
    void setUp() {
        manager.begin();
        repo.save(feeProvider1);
        repo.save(feeProvider2);
        repo.save(feeProvider3);
        repo.save(feeProvider4);
        manager.commit();
    }

    @Test
    void save() {
        manager.begin();
        repo.save(feeProvider5);
        manager.commit();
        FeeProvider feeProvider = repo.get(feeProvider5.getId());
        assertEquals(feeProvider5, feeProvider);
    }

    @Test
    void get() {
        FeeProvider feeProvider = repo.get(feeProvider2.getId());
        assertEquals(feeProvider2, feeProvider);
    }

    @Test
    void getByAccount() {
        List<FeeProvider> byAccount = repo.getByAccount(alice);
        assertEquals(List.of(feeProvider1, feeProvider3, feeProvider4), byAccount);
    }
}