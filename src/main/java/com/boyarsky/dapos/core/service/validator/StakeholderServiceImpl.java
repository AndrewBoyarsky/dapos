package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.validator.VoteRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class StakeholderServiceImpl implements StakeholderService {
    private VoteRepository repository;
    private AccountService accountService;
    private LedgerService ledgerService;
    private BlockchainConfig blockchainConfig;

    @Autowired
    public StakeholderServiceImpl(VoteRepository repository, AccountService accountService, LedgerService ledgerService, BlockchainConfig blockchainConfig) {
        this.repository = repository;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.blockchainConfig = blockchainConfig;
    }

    @Override
    public StakeholderPunishmentData punishStakeholders(AccountId validatorId, long height) {
        List<VoteEntity> votes = repository.getAllVotesForValidator(validatorId);
        long totalPunishment = 0;
        int removed = 0;
        for (VoteEntity vote : votes) {
            long resultStakeToBeSaved = imposeFine(vote.getTotalPower(), blockchainConfig.getAbsentPunishment());
            long prevStake = vote.getTotalPower();
            if (prevStake != resultStakeToBeSaved) {
                ledgerService.add(new LedgerRecord(height, -prevStake + resultStakeToBeSaved, LedgerRecord.Type.ABSENT_VOTER_FINE.toString(), validatorId, vote.getAccountId(), height));
                vote.setTotalPower(resultStakeToBeSaved);
                vote.setHeight(height);
                totalPunishment += (prevStake - resultStakeToBeSaved);
                long refundAmount = save(vote, LedgerRecord.Type.ABSENT_VOTER_AUTO_REVOCATION);
                if (refundAmount != 0) {
                    removed++;
                }
                totalPunishment += refundAmount;
            }
        }
        return new StakeholderPunishmentData(totalPunishment, removed);
    }

    long save(VoteEntity vote, LedgerRecord.Type event) {
        long refunded = 0;
        if (vote.getTotalPower() <= blockchainConfig.getMinVoteStake()) {
            refunded = remove(vote, event);
        } else {
            repository.save(vote);
        }
        return refunded;
    }

    long remove(VoteEntity vote, LedgerRecord.Type event) {
        repository.remove(vote.getValidatorId(), vote.getAccountId());
        Operation operation = new Operation(vote.getHeight(), vote.getHeight(), event.toString(), vote.getTotalPower());
        accountService.addToBalance(vote.getAccountId(), vote.getValidatorId(), operation);
        return vote.getTotalPower();
    }

    @Override
    public StakeholderPunishmentData punishByzantineStakeholders(AccountId validatorId, long height) {
        List<VoteEntity> votes = repository.getAllVotesForValidator(validatorId);
        long totalPunishment = 0;
        int removed = votes.size();
        for (VoteEntity vote : votes) {
            BigDecimal punishmentPercent = blockchainConfig.getByzantinePunishment();
            long resultStakeToBeSaved = imposeFine(vote.getTotalPower(), punishmentPercent);
            long prevStake = vote.getTotalPower();
            long fine = prevStake - resultStakeToBeSaved;
            vote.setTotalPower(resultStakeToBeSaved);
            vote.setHeight(height);
            ledgerService.add(new LedgerRecord(height, -fine, LedgerRecord.Type.VOTER_BYZANTINE_FINE.toString(), validatorId, vote.getAccountId(), height));
            remove(vote, LedgerRecord.Type.VOTER_BYZANTINE_AUTO_REVOCATION);
            totalPunishment += fine;
        }
        return new StakeholderPunishmentData(totalPunishment, removed);
    }

    long imposeFine(long stake, BigDecimal punishmentPercent) {
        BigDecimal totalStake = BigDecimal.valueOf(stake);
        BigDecimal punishmentAmount = punishmentPercent.multiply(totalStake).divide(BigDecimal.valueOf(100), RoundingMode.DOWN);
        BigDecimal resultStake = totalStake.subtract(punishmentAmount);
        return resultStake.toBigInteger().longValueExact();
    }

    @Override
    public long voteFor(AccountId validatorId, AccountId voterId, long votePower, long height) {
        long votes = repository.countAllVotesForValidator(validatorId);
        long stakeDiff;
        if (votes >= blockchainConfig.getMaxValidatorVotes()) {
            VoteEntity voteEntity = repository.minVoteForValidator(validatorId);
            voteEntity.setHeight(height);
            remove(voteEntity, LedgerRecord.Type.VOTE_SUPERSEDED);
            stakeDiff = votePower - voteEntity.getTotalPower();
        } else {
            stakeDiff = votePower;
        }
        VoteEntity entity = repository.getBy(validatorId, voterId);
        if (entity == null) {
            entity = new VoteEntity();
            entity.setAccountId(voterId);
            entity.setValidatorId(validatorId);
        }
        entity.setHeight(height);
        entity.setTotalPower(entity.getTotalPower() + votePower);
        return stakeDiff;
    }

    @Override
    public long revokeVote(AccountId validatorId, AccountId voterId, long height) {
        VoteEntity entity = repository.getBy(validatorId, voterId);
        accountService.addToBalance(voterId, validatorId, new Operation(height, height, "REVOKE", entity.getTotalPower()));
        repository.remove(validatorId, voterId);
        return entity.getTotalPower();
    }

    @Override
    public void distributeRewardForValidator(AccountId id, long stakeholdersReward, long height) {
        List<VoteEntity> votes = repository.getAllVotesForValidator(id);
        long totalStake = 0;
        for (VoteEntity vote : votes) {
            totalStake += vote.getTotalPower();
        }
        for (VoteEntity vote : votes) {
            BigDecimal votePercent = BigDecimal.valueOf(vote.getTotalPower()).divide(BigDecimal.valueOf(totalStake), 8, RoundingMode.DOWN);
            BigDecimal voteReward = BigDecimal.valueOf(stakeholdersReward).multiply(votePercent);
            accountService.addToBalance(vote.getAccountId(), vote.getValidatorId(), new Operation(height, height, "VOTE REWARD", voteReward.toBigInteger().longValueExact()));
            vote.setHeight(height);
            repository.save(vote);
        }
    }

    @Override
    public boolean exists(AccountId validator, AccountId voter) {
        return false;
    }

    @Override
    public long minStake(AccountId validator) {
        return repository.minVoteForValidator(validator).getTotalPower();
    }
}
