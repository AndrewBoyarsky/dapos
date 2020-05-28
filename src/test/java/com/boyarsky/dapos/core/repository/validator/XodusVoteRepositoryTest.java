package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ContextConfiguration(classes = {RepoTest.Config.class, XodusVoteRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.validator")
class XodusVoteRepositoryTest extends RepoTest {
    AccountId voter1 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter2 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter3 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId voter4 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId validator1 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validator2 = TestUtil.generateValidatorAcc().getCryptoId();
    VoteEntity v1 = new VoteEntity(100L, voter1, validator1, 1000);
    VoteEntity v2 = new VoteEntity(120L, voter2, validator1, 382);
    VoteEntity v3 = new VoteEntity(120L, voter3, validator1, 289);
    VoteEntity v4 = new VoteEntity(90L, voter2, validator2, 388);
    VoteEntity v5 = new VoteEntity(122L, voter4, validator2, 433);

    @Autowired
    XodusVoteRepository voteRepository;

    @BeforeEach
    void setUp() {
        manager.begin();
        voteRepository.save(v1);
        voteRepository.save(v2);
        voteRepository.save(v3);
        voteRepository.save(v4);
        voteRepository.save(v5);
        manager.commit();
    }

    @Test
    void update() {
        manager.begin();
        v5.setHeight(33333);
        v5.setTotalPower(v5.getTotalPower() + 10);
        voteRepository.save(v5);
        manager.commit();
        assertEquals(5, voteRepository.getAll().size());
        assertEquals(v5, voteRepository.getBy(v5.getValidatorId(), v5.getAccountId()));
    }

    @Test
    void remove() {
        manager.begin();
        voteRepository.remove(v3);
        manager.commit();

        VoteEntity entity = voteRepository.getBy(v3.getValidatorId(), v3.getAccountId());
        assertNull(entity);
        List<VoteEntity> all = voteRepository.getAll();
        assertEquals(4, all.size());
    }

    @Test
    void getBy() {
        VoteEntity entity = voteRepository.getBy(v3.getValidatorId(), v3.getAccountId());
        assertEquals(v3, entity);
    }

    @Test
    void getAllVotesForValidator() {
        List<VoteEntity> votes = voteRepository.getAllVotesForValidator(validator2);
        assertEquals(List.of(v5, v4), votes);
    }

    @Test
    void countAllVotesForValidator() {
        long votes1 = voteRepository.countAllVotesForValidator(validator1);
        long votes2 = voteRepository.countAllVotesForValidator(validator2);

        assertEquals(3, votes1);
        assertEquals(2, votes2);
    }

    @Test
    void minVoteForValidator() {
        VoteEntity minVote = voteRepository.minVoteForValidator(validator1);

        assertEquals(v3, minVote);
    }

    @Test
    void getAllVotesForVoter() {
        List<VoteEntity> votes = voteRepository.getAllVotesForVoter(voter2);

        assertEquals(List.of(v2, v4), votes);
    }
}