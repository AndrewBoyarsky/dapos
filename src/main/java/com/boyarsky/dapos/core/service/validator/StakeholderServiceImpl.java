package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.validator.VoteRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;
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
    public StakeholderServiceImpl(VoteRepository repository, AccountService accountService) {
        this.repository = repository;
        this.accountService = accountService;
    }

    @Override
    public StakeholderPunishmentData punishStakeholders(AccountId validatorId, long height) {
        List<VoteEntity> votes = repository.getAllVotesForValidator(validatorId);
        long totalPunishment = 0;
        int removed = 0;
        for (VoteEntity vote : votes) {
            long resultStakeToBeSaved = imposeFine(vote.getTotalStake(), blockchainConfig.getCurrentConfig().getAbsentPunishment());
            long prevStake = vote.getTotalStake();
            if (prevStake != resultStakeToBeSaved) {
                ledgerService.add(new LedgerRecord(height, -prevStake + resultStakeToBeSaved, LedgerRecord.Type.ABSENT_VOTER_FINE, validatorId, vote.getAccountId()));
                vote.setTotalStake(resultStakeToBeSaved);
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
        if (vote.getTotalStake() <= blockchainConfig.getCurrentConfig().getMinVoteStake()) {
            refunded = remove(vote, event);
        } else {
            repository.save(vote);
        }
        return refunded;
    }

    long remove(VoteEntity vote, LedgerRecord.Type event) {
        repository.remove(vote);
        accountService.addToBalance(vote.getAccountId(), vote.getTotalStake(), vote.getHeight());
        ledgerService.add(new LedgerRecord(vote.getHeight(), vote.getTotalStake(), event, vote.getValidatorId(), vote.getAccountId()));
        return vote.getTotalStake();
    }

    @Override
    public StakeholderPunishmentData punishByzantineStakeholders(AccountId validatorId, long height) {
        List<VoteEntity> votes = repository.getAllVotesForValidator(validatorId);
        long totalPunishment = 0;
        int removed = votes.size();
        for (VoteEntity vote : votes) {
            BigDecimal punishmentPercent = blockchainConfig.getCurrentConfig().getByzantinePunishment();
            long resultStakeToBeSaved = imposeFine(vote.getTotalStake(), punishmentPercent);
            long prevStake = vote.getTotalStake();
            long fine = prevStake - resultStakeToBeSaved;
            vote.setTotalStake(resultStakeToBeSaved);
            vote.setHeight(height);
            ledgerService.add(new LedgerRecord(height, -fine, LedgerRecord.Type.VOTER_BYZANTINE_FINE, validatorId, vote.getAccountId()));
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
    public long voteFor(Transaction tx, VoteAttachment attachment) {
        long voteStake = tx.getAmount();
        long votes = repository.countAllVotesForValidator(tx.getRecipient());
        long stakeDiff = 0;
        if (votes == blockchainConfig.getCurrentConfig().getMaxValidatorVotes()) {
            VoteEntity voteEntity = repository.minVoteForValidator(tx.getRecipient());
            voteEntity.setHeight(tx.getHeight());
            remove(voteEntity, LedgerRecord.Type.VOTE_SUPERSEDED);
            stakeDiff = tx.getAmount() - voteEntity.getTotalStake();
        }
        VoteEntity entity = repository.getBy(tx.getRecipient(), tx.getSender());
        if (entity == null) {
            entity = new VoteEntity();
            entity.setAccountId(tx.getSender());
            entity.setValidatorId(tx.getRecipient());
        }
        entity.setHeight(tx.getHeight());
        entity.setTotalStake(entity.getTotalStake() + voteStake);
        return stakeDiff;
    }

    @Override
    public long revokeVote(AccountId validator, AccountId voter, long height) {
        return 0;
    }

    @Override
    public void distributeRewardForValidator(AccountId id, BigDecimal stakeholdersReward) {

    }

    @Override
    public boolean exists(AccountId validator, AccountId voter) {
        return false;
    }

    @Override
    public long minStake(AccountId validator) {
        return repository.minVoteForValidator(validator).getTotalStake();
    }
}
