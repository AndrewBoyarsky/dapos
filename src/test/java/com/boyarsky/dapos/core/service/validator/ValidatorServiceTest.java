package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.validator.ValidatorRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTest {
    @Mock
    ValidatorRepository vRepository;
    @Mock
    AccountService aService;
    @Mock
    LedgerService lService;
    @Mock
    VoterService sService;
    @Mock
    BlockchainConfig blockchainConfig;

    ValidatorService service;
    Account validatorAcc1 = TestUtil.generateValidatorAcc();
    Account validatorAcc2 = TestUtil.generateValidatorAcc();
    Account validatorAcc3 = TestUtil.generateValidatorAcc();
    Account validatorAcc4 = TestUtil.generateValidatorAcc();
    Account validatorAcc5 = TestUtil.generateValidatorAcc();
    Account validatorAcc6 = TestUtil.generateValidatorAcc();
    AccountId rewardAddress = TestUtil.generateEd25Acc().getCryptoId();
    AccountId rewardAddress2 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId rewardAddress3 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId rewardAddress4 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId rewardAddress5 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId rewardAddress6 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter1 = TestUtil.generateEd25Acc().getCryptoId();

    @BeforeEach
    void setUp() {
        service = new ValidatorServiceImpl(vRepository, blockchainConfig, lService, sService, aService);
    }

    @Test
    void registerValidator() {

        ValidatorEntity entity = service.registerValidator(validatorAcc1.getPublicKey(), 10, rewardAddress, true, 100);

        ValidatorEntity expected = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 0, validatorAcc1.getCryptoId(), rewardAddress, 0, 0, 10);
        expected.setHeight(100);
        assertEquals(expected, entity);
        verify(vRepository).save(entity);
    }

    @Test
    void getAllUpdated() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 1, validatorAcc1.getCryptoId(), rewardAddress, 1, 1, 0);
        ValidatorEntity v2 = new ValidatorEntity(true, validatorAcc2.getPublicKey(), 2, validatorAcc1.getCryptoId(), rewardAddress, 100, 234, 1000);
        List<ValidatorEntity> entities = List.of(v1, v2);
        doReturn(entities).when(vRepository).getAll(100);

        List<ValidatorEntity> allUpdated = service.getAll(100);
        assertEquals(allUpdated, entities);
    }

    @Test
    void toggleValidator() {
        ValidatorEntity v1 = new ValidatorEntity(false, validatorAcc2.getPublicKey(), 1, validatorAcc2.getCryptoId(), rewardAddress, 1, 1, 0);
        doReturn(v1).when(vRepository).getById(validatorAcc2.getCryptoId());

        service.toggleValidator(validatorAcc2.getCryptoId(), true, 100);

        assertEquals(100, v1.getHeight());
        assertTrue(v1.isEnabled());
        verify(vRepository).save(v1);
    }

    @Test
    void punishByzantine() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 0, validatorAcc1.getCryptoId(), rewardAddress, 10, 100, 10);
        doReturn(v1).when(vRepository).getById(validatorAcc1.getCryptoId());

        doReturn(new StakeholderPunishmentData(20, 0, 10)).when(sService).punishByzantineStakeholders(validatorAcc1.getCryptoId(), 1000);
        long punishment = service.punishByzantine(validatorAcc1.getCryptoId(), 1000);
        assertEquals(20, punishment);
        verifyByzantineValidator(v1, 20, 1000);
    }

    void verifyByzantineValidator(ValidatorEntity v, long punishment, long height) {
        verify(vRepository).save(v);
        verify(lService).add(new LedgerRecord(height, -punishment, "VALIDATOR_BYZANTINE_FINE", null, v.getId(), height));
        assertFalse(v.isEnabled());
        assertEquals(height, v.getHeight());
        assertEquals(0, v.getVotePower());
        assertEquals(0, v.getVotes());
    }

    @Test
    void punishByzantine_notEnabled() {
        ValidatorEntity v1 = new ValidatorEntity(false, validatorAcc1.getPublicKey(), 0, validatorAcc1.getCryptoId(), rewardAddress, 10, 100, 10);
        doReturn(v1).when(vRepository).getById(validatorAcc1.getCryptoId());

        long punishment = service.punishByzantine(validatorAcc1.getCryptoId(), 1000);
        assertEquals(0, punishment);
        verify(vRepository, never()).save(v1);
        verify(sService, never()).punishStakeholders(validatorAcc1.getCryptoId(), 1000);
    }

    @Test
    void testPunishManyAbsent() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 3, validatorAcc1.getCryptoId(), rewardAddress, 10, 120, 0);
        ValidatorEntity v2 = new ValidatorEntity(false, validatorAcc2.getPublicKey(), 3, validatorAcc2.getCryptoId(), rewardAddress2, 111, 150, 90);
        ValidatorEntity v3 = new ValidatorEntity(true, validatorAcc3.getPublicKey(), 5, validatorAcc3.getCryptoId(), rewardAddress3, 232, 90, 1000);
        ValidatorEntity v4 = new ValidatorEntity(true, validatorAcc4.getPublicKey(), 5, validatorAcc4.getCryptoId(), rewardAddress4, 100, 200, 5000);
        List<ValidatorEntity> vs = List.of(v1, v2, v3, v4);
        doReturn(6L).when(blockchainConfig).getAbsentPeriod();
        doReturn(new StakeholderPunishmentData(21, 0, 2)).when(sService).punishStakeholders(validatorAcc3.getCryptoId(), 1000);
        doReturn(new StakeholderPunishmentData(46, 0, 3)).when(sService).punishStakeholders(validatorAcc4.getCryptoId(), 1000);
        for (ValidatorEntity v : vs) {
            doReturn(v).when(vRepository).getById(v.getId());
        }
        long punishment = service.punishAbsents(Set.of(validatorAcc1.getCryptoId(), validatorAcc2.getCryptoId(), validatorAcc3.getCryptoId(), validatorAcc4.getCryptoId()), 1000);
        assertEquals(67, punishment);
        verifyPunishedAbsentValidator(v3, 69, 21, 230, 1000);
        verifyPunishedAbsentValidator(v4, 154, 46, 97, 1000);
        verifyWarnedAbsentValidator(v1, 1000);
    }

    void verifyPunishedAbsentValidator(ValidatorEntity v, long power, long punishment, long votes, long height) {
        verify(vRepository).save(v);
        assertEquals(power, v.getVotePower());
        assertEquals(votes, v.getVotes());
        verify(lService).add(new LedgerRecord(v.getHeight(), -punishment, "ABSENT_VALIDATOR_FINE", null, v.getId(), height));
    }

    void verifyWarnedAbsentValidator(ValidatorEntity v, long height) {
        verify(vRepository).save(v);
        verify(lService).add(new LedgerRecord(v.getHeight(), v.getAbsentFor(), "ABSENT_VALIDATOR", null, v.getId(), height));
    }


    @Test
    void testPunishManyByzantine() {
        AccountId vid1 = validatorAcc1.getCryptoId();
        AccountId vid2 = validatorAcc2.getCryptoId();
        AccountId vid3 = validatorAcc3.getCryptoId();
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 0, vid1, rewardAddress, 102, 500, 0);
        ValidatorEntity v2 = new ValidatorEntity(true, validatorAcc2.getPublicKey(), 1, vid2, rewardAddress2, 212, 120, 100);
        ValidatorEntity v3 = new ValidatorEntity(true, validatorAcc3.getPublicKey(), 2, vid3, rewardAddress3, 100, 20, 10000);
        List<ValidatorEntity> vs = List.of(v1, v2, v3);
        doReturn(new StakeholderPunishmentData(50, 450, 102)).when(sService).punishByzantineStakeholders(vid1, 100);
        doReturn(new StakeholderPunishmentData(12, 108, 212)).when(sService).punishByzantineStakeholders(vid2, 100);
        doReturn(new StakeholderPunishmentData(2, 18, 100)).when(sService).punishByzantineStakeholders(vid3, 100);
        for (ValidatorEntity v : vs) {
            doReturn(v).when(vRepository).getById(v.getId());
        }
        long punishment = service.punishByzantines(Set.of(vid1, vid2, vid3), 100);

        assertEquals(64, punishment);
        verifyByzantineValidator(v1, 50, 100);
        verifyByzantineValidator(v2, 12, 100);
        verifyByzantineValidator(v3, 2, 100);

    }


    @Test
    void punishByzantine_nonExistent() {
        long punishment = service.punishByzantine(validatorAcc1.getCryptoId(), 1000);
        assertEquals(0, punishment);
        verify(vRepository, never()).save(any());
        verify(sService, never()).punishStakeholders(any(AccountId.class), any(Long.class));
    }

    @Test
    void revoke() {
        ValidatorEntity v1 = new ValidatorEntity(false, validatorAcc1.getPublicKey(), 10, validatorAcc1.getCryptoId(), rewardAddress, 2000, 10500, 500);
        doReturn(v1).when(vRepository).getById(validatorAcc1.getCryptoId());
        doReturn(9000L).when(sService).revokeVote(validatorAcc1.getCryptoId(), voter1, 200);
        service.revoke(validatorAcc1.getCryptoId(), voter1, 200);

        verify(vRepository).save(v1);
        assertEquals(1999, v1.getVotes());
        assertEquals(1500, v1.getVotePower());
        assertEquals(200, v1.getHeight());

    }

    @Test
    void punishAbsent_fine() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 9, validatorAcc1.getCryptoId(), rewardAddress, 2000, 15000, 500);
        doReturn(v1).when(vRepository).getById(validatorAcc1.getCryptoId());
        doReturn(new StakeholderPunishmentData(2000L, 0, 2)).when(sService).punishStakeholders(validatorAcc1.getCryptoId(), 200);
        doReturn(10L).when(blockchainConfig).getAbsentPeriod();

        long l = service.punishAbsent(validatorAcc1.getCryptoId(), 200);
        assertEquals(2000L, l);
        verifyPunishedAbsentValidator(v1, 13000L, 2000L, 1998, 200);
    }

    @Test
    void punishAbsent_warning() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc2.getPublicKey(), 1, validatorAcc2.getCryptoId(), rewardAddress, 2, 3, 100);
        doReturn(v1).when(vRepository).getById(validatorAcc2.getCryptoId());
        doReturn(3L).when(blockchainConfig).getAbsentPeriod();

        long l = service.punishAbsent(validatorAcc2.getCryptoId(), 300);
        assertEquals(l, 0);
        verifyWarnedAbsentValidator(v1, 300);
    }

    @Test
    void punishAbsent_notEnabled() {
        ValidatorEntity v1 = new ValidatorEntity(false, validatorAcc2.getPublicKey(), 1, validatorAcc2.getCryptoId(), rewardAddress, 2, 3, 100);
        doReturn(v1).when(vRepository).getById(validatorAcc2.getCryptoId());

        long l = service.punishAbsent(validatorAcc2.getCryptoId(), 300);
        assertEquals(l, 0);
        verify(vRepository, never()).save(any(ValidatorEntity.class));
        verifyNoInteractions(lService);
    }

    @Test
    void punishAbsent_nonExistent() {
        long l = service.punishAbsent(validatorAcc2.getCryptoId(), 300);
        assertEquals(l, 0);
        verify(vRepository, never()).save(any(ValidatorEntity.class));
        verifyNoInteractions(lService);
    }

    @Test
    void distributeReward() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 1, validatorAcc1.getCryptoId(), rewardAddress, 1, 1, 0);
        ValidatorEntity v2 = new ValidatorEntity(true, validatorAcc2.getPublicKey(), 3, validatorAcc2.getCryptoId(), rewardAddress2, 111, 100, 1111);
        ValidatorEntity v3 = new ValidatorEntity(true, validatorAcc3.getPublicKey(), 4, validatorAcc3.getCryptoId(), rewardAddress3, 232, 12, 1);
        ValidatorEntity v4 = new ValidatorEntity(true, validatorAcc4.getPublicKey(), 5, validatorAcc4.getCryptoId(), rewardAddress4, 144, 50, 10000);
        ValidatorEntity v5 = new ValidatorEntity(false, validatorAcc5.getPublicKey(), 0, validatorAcc5.getCryptoId(), rewardAddress5, 10, 50, 500);
        ValidatorEntity v6 = new ValidatorEntity(true, validatorAcc6.getPublicKey(), 0, validatorAcc6.getCryptoId(), rewardAddress6, 20, 20, 1000);

        List<ValidatorEntity> validators = List.of(v1, v2, v3, v4, v5, v6);
        for (ValidatorEntity validator : validators) {
            doReturn(validator).when(vRepository).getById(validator.getId());
        }
        doReturn(4L).when(blockchainConfig).getMaxValidators();

        service.distributeReward(validators.stream().map(ValidatorEntity::getId).collect(Collectors.toSet()), 300, 100);

        List<ValidatorEntity> filtered = List.of(v2, v3, v4, v6);
        filtered.forEach(e -> {
            if (e.getAbsentFor() != 0) {
                verify(vRepository).save(e);
            }
            assertEquals(0, e.getAbsentFor());
        });
        verify(aService).addToBalance(rewardAddress2, validatorAcc2.getCryptoId(), new Operation(100, 100, "VALIDATOR_BLOCK_REWARD_FEE", 18));
        verify(aService).addToBalance(rewardAddress3, validatorAcc3.getCryptoId(), new Operation(100, 100, "VALIDATOR_BLOCK_REWARD_FEE", 0));
        verify(aService).addToBalance(rewardAddress4, validatorAcc4.getCryptoId(), new Operation(100, 100, "VALIDATOR_BLOCK_REWARD_FEE", 82));
        verify(aService).addToBalance(rewardAddress6, validatorAcc6.getCryptoId(), new Operation(100, 100, "VALIDATOR_BLOCK_REWARD_FEE", 3));
        verify(sService).distributeRewardForValidator(validatorAcc2.getCryptoId(), 146, 100);
        verify(sService).distributeRewardForValidator(validatorAcc3.getCryptoId(), 19, 100);
        verify(sService).distributeRewardForValidator(validatorAcc4.getCryptoId(), 0, 100);
        verify(sService).distributeRewardForValidator(validatorAcc6.getCryptoId(), 29, 100);

    }

    @Test
    void addVote_new() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 10, validatorAcc1.getCryptoId(), rewardAddress, 1, 10, 1);
        doReturn(v1).when(vRepository).getById(v1.getId());
        doReturn(1000L).when(sService).voteFor(v1.getId(), voter1, 1000, 200);
        doReturn(3L).when(blockchainConfig).getMaxValidatorVotes();

        service.addVote(v1.getId(), voter1, 1000, 200);

        verify(vRepository).save(v1);
        assertEquals(1010, v1.getVotePower());
        assertEquals(2, v1.getVotes());
        assertEquals(200, v1.getHeight());
    }


    @Test
    void addVote_replace() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 10, validatorAcc1.getCryptoId(), rewardAddress, 10, 1032, 1);
        doReturn(v1).when(vRepository).getById(v1.getId());
        doReturn(1000L).when(sService).voteFor(v1.getId(), voter1, 5000, 200);
        doReturn(10L).when(blockchainConfig).getMaxValidatorVotes();

        service.addVote(v1.getId(), voter1, 5000, 200);

        verify(vRepository).save(v1);
        assertEquals(2032, v1.getVotePower());
        assertEquals(10, v1.getVotes());
        assertEquals(200, v1.getHeight());
    }

    @Test
    void get() {
        ValidatorEntity v1 = new ValidatorEntity(true, validatorAcc1.getPublicKey(), 10, validatorAcc1.getCryptoId(), rewardAddress, 10, 1032, 1);
        doReturn(v1).when(vRepository).getById(v1.getId());

        ValidatorEntity entity = service.get(v1.getId());

        assertEquals(v1, entity);
    }
}