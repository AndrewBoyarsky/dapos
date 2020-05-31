package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.validator.VoteRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class VoterServiceImplTest {
    @Mock
    VoteRepository repository;
    @Mock
    AccountService accountService;
    @Mock
    LedgerService ledgerService;
    @Mock
    BlockchainConfig blockchainConfig;

    VoterService service;

    AccountId validatorId1 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validatorId2 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId voter1 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter2 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter3 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter4 = TestUtil.generateEd25Acc().getCryptoId();

    @BeforeEach
    void setUp() {
        service = new VoterServiceImpl(repository, accountService, ledgerService, blockchainConfig);
    }

    @Test
    void punishStakeholders() {
        VoteEntity vote1 = new VoteEntity(1, voter1, validatorId1, 1000);
        VoteEntity vote2 = new VoteEntity(1, voter2, validatorId1, 666);
        VoteEntity vote3 = new VoteEntity(1, voter3, validatorId1, 1500);
        VoteEntity vote4 = new VoteEntity(12, voter4, validatorId1, 80);
        List<VoteEntity> votes = List.of(vote1, vote2, vote3, vote4);
        doReturn(votes).when(repository).getAllVotesForValidator(validatorId1);
        doReturn(new BigDecimal("1.2")).when(blockchainConfig).getAbsentPunishment();
        doReturn(659L).when(blockchainConfig).getMinVoteStake();

        StakeholderPunishmentData data = service.punishStakeholders(validatorId1, 300);

        assertEquals(2, data.getRemoved());
        assertEquals(39, data.getBurned());
        assertEquals(737, data.getRevoked());
        verify(repository).remove(vote2);
        verify(repository).remove(vote4);
        verify(accountService).addToBalance(voter2, validatorId1, new Operation(300, 300, "ABSENT_VOTER_AUTO_REVOCATION", 658));
        verify(accountService).addToBalance(voter4, validatorId1, new Operation(300, 300, "ABSENT_VOTER_AUTO_REVOCATION", 79));
        verify(ledgerService).add(new LedgerRecord(300, -8, "ABSENT_VOTER_FINE", validatorId1, voter2, 300));
        verify(ledgerService).add(new LedgerRecord(300, -1, "ABSENT_VOTER_FINE", validatorId1, voter4, 300));
        verify(ledgerService).add(new LedgerRecord(300, -12, "ABSENT_VOTER_FINE", validatorId1, voter1, 300));
        verify(ledgerService).add(new LedgerRecord(300, -18, "ABSENT_VOTER_FINE", validatorId1, voter3, 300));
        verify(repository).save(vote1);
        verify(repository).save(vote3);
        verify(repository, never()).save(vote4);
        assertEquals(1482, vote3.getTotalPower());
        assertEquals(988, vote1.getTotalPower());
    }

    @Test
    void punishByzantineStakeholders() {
        VoteEntity vote1 = new VoteEntity(1, voter1, validatorId1, 250);
        VoteEntity vote2 = new VoteEntity(1, voter2, validatorId1, 150);
        VoteEntity vote3 = new VoteEntity(1, voter3, validatorId1, 121);
        VoteEntity vote4 = new VoteEntity(12, voter4, validatorId1, 90);
        List<VoteEntity> votes = List.of(vote1, vote2, vote3, vote4);
        doReturn(votes).when(repository).getAllVotesForValidator(validatorId1);
        doReturn(new BigDecimal("5.45")).when(blockchainConfig).getByzantinePunishment();

        StakeholderPunishmentData data = service.punishByzantineStakeholders(validatorId1, 300);
        assertEquals(4, data.getRemoved());
        assertEquals(35, data.getBurned());
        assertEquals(576, data.getRevoked());
        verifyRemovedVote(vote1, 236, 14, 300);
        verifyRemovedVote(vote2, 141, 9, 300);
        verifyRemovedVote(vote3, 114, 7, 300);
        verifyRemovedVote(vote4, 85, 5, 300);
    }

    void verifyRemovedVote(VoteEntity voteEntity, long resultStake, long punishment, int height) {
        verify(repository).remove(voteEntity);
        verify(ledgerService).add(new LedgerRecord(height, -punishment, "VOTER_BYZANTINE_FINE", voteEntity.getValidatorId(), voteEntity.getAccountId(), height));
        // such action implied by accountService
        //        verify(ledgerService).add(new LedgerRecord(height, punishment, "VOTER_BYZANTINE_AUTO_REVOCATION", voteEntity.getValidatorId(), voteEntity.getAccountId(), height));
        assertEquals(voteEntity.getTotalPower(), resultStake);
        verify(accountService).addToBalance(voteEntity.getAccountId(), voteEntity.getValidatorId(), new Operation(height, height, "VOTER_BYZANTINE_AUTO_REVOCATION", resultStake));
    }

    @Test
    void voteFor_new() {
        VoteEntity vote1 = new VoteEntity(101, voter1, validatorId1, 200);
        doReturn(10L).when(blockchainConfig).getMaxValidatorVotes();
        doReturn(9L).when(repository).countAllVotesForValidator(validatorId1);

        long stake = service.voteFor(validatorId1, voter1, 200, 101);
        verify(repository).save(vote1);
        verify(accountService).addToBalance(voter1, validatorId1, new Operation(101, 101, "VOTE", -200));
        assertEquals(200, stake);
    }

    @Test
    void voteFor_replace() {
        VoteEntity vote1 = new VoteEntity(101, voter1, validatorId1, 200);
        VoteEntity existing = new VoteEntity(90, voter2, validatorId1, 15);
        doReturn(existing).when(repository).minVoteForValidator(validatorId1);
        doReturn(10L).when(blockchainConfig).getMaxValidatorVotes();
        doReturn(10L).when(repository).countAllVotesForValidator(validatorId1);

        long stake = service.voteFor(validatorId1, voter1, 200, 101);
        assertEquals(185, stake);
        verify(repository).save(vote1);
        verify(accountService).addToBalance(voter1, validatorId1, new Operation(101, 101, "VOTE", -200));
    }

    @Test
    void voteFor_addExistent() {
        VoteEntity vote1 = new VoteEntity(101, voter1, validatorId1, 200);
        doReturn(vote1).when(repository).getBy(validatorId1, voter1);
        doReturn(10L).when(blockchainConfig).getMaxValidatorVotes();
        doReturn(2L).when(repository).countAllVotesForValidator(validatorId1);

        long stake = service.voteFor(validatorId1, voter1, 400, 200);
        assertEquals(400, stake);
        verify(repository).save(vote1);
        verify(accountService).addToBalance(voter1, validatorId1, new Operation(200, 200, "VOTE", -400));
        assertEquals(600, vote1.getTotalPower());
    }

    @Test
    void revokeVote() {
        VoteEntity vote1 = new VoteEntity(100, voter1, validatorId1, 500);
        doReturn(vote1).when(repository).getBy(validatorId1, voter1);

        long stake = service.revokeVote(validatorId1, voter1, 200);

        assertEquals(500, stake);
        verify(repository).remove(vote1);
        verify(accountService).addToBalance(voter1, validatorId1, new Operation(200, 200, "VOTE_REVOKED", 500));
    }

    @Test
    void distributeRewardForValidator() {
        VoteEntity vote1 = new VoteEntity(12, voter1, validatorId2, 922);
        VoteEntity vote2 = new VoteEntity(15, voter2, validatorId2, 9);
        VoteEntity vote3 = new VoteEntity(15, voter3, validatorId2, 243);
        VoteEntity vote4 = new VoteEntity(17, voter4, validatorId2, 100);
        List<VoteEntity> votes = List.of(vote1, vote2, vote3, vote4);
        doReturn(votes).when(repository).getAllVotesForValidator(validatorId2);

        service.distributeRewardForValidator(validatorId2, 125, 20);

        verify(accountService).addToBalance(voter1, validatorId2, new Operation(20, 20, "VOTE_REWARD", 90));
        verify(accountService).addToBalance(voter3, validatorId2, new Operation(20, 20, "VOTE_REWARD", 23));
        verify(accountService).addToBalance(voter4, validatorId2, new Operation(20, 20, "VOTE_REWARD", 9));
        verifyNoMoreInteractions(accountService);

    }

    @Test
    void exists() {
        VoteEntity vote1 = new VoteEntity(12, voter1, validatorId2, 922);
        doReturn(vote1).when(repository).getBy(validatorId2, voter1);

        boolean exist = service.exists(validatorId2, voter1);

        assertTrue(exist);
    }

    @Test
    void notExist() {
        boolean notExist = service.exists(validatorId1, voter2);

        assertFalse(notExist);
    }

    @Test
    void minStake() {
        VoteEntity minVote = new VoteEntity(100, voter2, validatorId1, 10);
        doReturn(minVote).when(repository).minVoteForValidator(validatorId1);

        long minStake = service.minStake(validatorId1);

        assertEquals(10, minStake);
    }
}