package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.validator.ValidatorRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private ValidatorRepository repository;
    private BlockchainConfig config;
    private LedgerService ledgerService;
    private StakeholderService stakeholderService;
    private AccountService accountService;

    @Autowired
    public ValidatorServiceImpl(ValidatorRepository repository, BlockchainConfig config, LedgerService ledgerService, StakeholderService stakeholderService, AccountService accountService) {
        this.repository = repository;
        this.config = config;
        this.ledgerService = ledgerService;
        this.stakeholderService = stakeholderService;
        this.accountService = accountService;
    }

    @Override
    public ValidatorEntity registerValidator(byte[] pubKey, int fee, AccountId rewardAddress, boolean enable, long height) {
        AccountId id = new AccountId(CryptoUtils.validatorAddress(pubKey));
        ValidatorEntity newValidator = new ValidatorEntity();
        newValidator.setEnabled(enable);
        newValidator.setId(id);
        newValidator.setPublicKey(pubKey);
        newValidator.setFee(fee);
        newValidator.setHeight(height);
        newValidator.setRewardId(rewardAddress);
        repository.save(newValidator);
        return newValidator;
    }

    @Override
    public List<ValidatorEntity> getAllUpdated(long height) {
        return repository.getAll(height);
    }

    @Override
    public void toggleValidator(@NonNull AccountId validatorId, boolean enabled, long height) {
        ValidatorEntity validatorEntity = get(validatorId);
        if (validatorEntity == null) {
            throw new IllegalArgumentException("Validator " + validatorId + " does not exist");
        }
        if (enabled) {
            validatorEntity.setAbsentFor(0);
        }
        validatorEntity.setEnabled(enabled);
        validatorEntity.setHeight(height);
        repository.save(validatorEntity);
    }

    @Override
    public long punishByzantine(AccountId validatorId, long height) {
        ValidatorEntity byId = repository.getById(validatorId);
        if (byId == null) {
            return 0;
        }
        if (!byId.isEnabled()) {
            return 0;
        }
        byId.setHeight(height);
        byId.setEnabled(false);
        long punishmentAmount = stakeholderService.punishByzantineStakeholders(validatorId, height).getBurned();
        ledgerService.add(new LedgerRecord(height, -punishmentAmount, LedgerRecord.Type.VALIDATOR_BYZANTINE_FINE.toString(), null, validatorId, height));
        byId.setVotePower(0);
        byId.setVotes(0);
        repository.save(byId);
        return punishmentAmount;
    }

    @Override
    public long punishByzantines(Set<AccountId> validators, long height) {
        long punishmentAmount = 0;
        for (AccountId validator : validators) {
            punishmentAmount += punishByzantine(validator, height);
        }
        return punishmentAmount;
    }

    @Override
    public void revoke(AccountId validatorId, AccountId voterId, long height) {
        long votePowerLoss = stakeholderService.revokeVote(validatorId, voterId, height);
        ValidatorEntity entity = get(validatorId);
        entity.setHeight(height);
        entity.setVotes(entity.getVotes() - 1);
        entity.setVotePower(entity.getVotePower() - votePowerLoss);
        repository.save(entity);
    }

    @Override
    public long punishAbsent(AccountId validatorId, long height) {
        ValidatorEntity byId = repository.getById(validatorId);
        if (byId == null) {
            return 0;
        }
        if (!byId.isEnabled()) {
            return 0;
        }
        long punishmentAmount = 0;
        byId.setHeight(height);
        byId.setAbsentFor(byId.getAbsentFor() + 1);
        if (byId.getAbsentFor() >= config.getAbsentPeriod()) {
            byId.setEnabled(false);
            StakeholderPunishmentData punishmentData = stakeholderService.punishStakeholders(validatorId, height);
            punishmentAmount = punishmentData.getBurned();
            ledgerService.add(new LedgerRecord(height, -punishmentAmount, LedgerRecord.Type.ABSENT_VALIDATOR_FINE.toString(), null, validatorId, height));
            byId.setVotePower(byId.getVotePower() - punishmentAmount);
            byId.setVotes(byId.getVotes() - punishmentData.getRemoved());
        } else {
            ledgerService.add(new LedgerRecord(height, byId.getAbsentFor(), LedgerRecord.Type.ABSENT_VALIDATOR.toString(), null, validatorId, height));
        }
        repository.save(byId);
        return punishmentAmount;
    }

    @Override
    public long punishAbsents(Set<AccountId> validators, long height) {
        long punishmentAmount = 0;
        for (AccountId validator : validators) {
            punishmentAmount += punishAbsent(validator, height);
        }
        return punishmentAmount;
    }

    @Override
    public void distributeReward(Set<AccountId> validators, long rewardAmount, long height) {
        List<ValidatorEntity> validatorEntities = validators
                .stream()
                .map(repository::getById)
                .filter(ValidatorEntity::isEnabled)
                .sorted(Comparator.comparing(ValidatorEntity::getVotePower).reversed())
                .limit(config.getMaxValidators())
                .collect(Collectors.toList());
        long totalVotePower = validatorEntities.stream().mapToLong(ValidatorEntity::getVotePower).sum();
        for (ValidatorEntity v : validatorEntities) {
            BigDecimal validatorPercent = BigDecimal.valueOf(v.getVotePower()).divide(BigDecimal.valueOf(totalVotePower), 8, RoundingMode.DOWN);
            BigDecimal validatorReward = validatorPercent.multiply(BigDecimal.valueOf(rewardAmount));
            BigDecimal validatorFee = validatorReward.multiply(BigDecimal.valueOf(v.getFee())).divide(BigDecimal.valueOf(10000), RoundingMode.DOWN);
            v.setHeight(height);
            if (v.getAbsentFor() != 0) {
                v.setAbsentFor(0);
                repository.save(v);
            }
            long validatorFeeRounded = validatorFee.toBigInteger().longValueExact();
            accountService.addToBalance(v.getRewardId(), v.getId(), new Operation(height, height, "VALIDATOR REWARD", validatorFeeRounded));
            long stakeholdersReward = validatorReward.subtract(validatorFee).toBigInteger().longValueExact();
            stakeholderService.distributeRewardForValidator(v.getId(), stakeholdersReward, height);
        }
    }

    @Override
    public void addVote(AccountId validatorId, AccountId voterId, long voterPower, long height) {
        ValidatorEntity byId = repository.getById(validatorId);
        if (byId.getVotes() != config.getMaxValidatorVotes()) {
            byId.setVotes(byId.getVotes() + 1);
        }
        byId.setHeight(height);
        long stakeDiff = stakeholderService.voteFor(validatorId, voterId, voterPower, height);
        byId.setVotePower(byId.getVotePower() + stakeDiff);
        repository.save(byId);
    }

    @Override
    public ValidatorEntity get(AccountId id) {
        return repository.getById(id);
    }

}
