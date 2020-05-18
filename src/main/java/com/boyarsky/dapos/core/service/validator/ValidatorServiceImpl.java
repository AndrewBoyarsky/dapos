package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.validator.ValidatorRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RegisterValidatorAttachment;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private ValidatorRepository repository;
    private BlockchainConfig config;
    private LedgerService ledgerService;
    private StakeholderService stakeholderService;
    private AccountService accountService;

    @Autowired
    public ValidatorServiceImpl(ValidatorRepository repository, BlockchainConfig config) {
        this.repository = repository;
        this.config = config;
    }

    @Override
    public void registerValidator(Transaction tx, RegisterValidatorAttachment attachment) {
        AccountId id = new AccountId(CryptoUtils.validatorAddress(attachment.getPublicKey()));
        ValidatorEntity newValidator = new ValidatorEntity();
        newValidator.setEnabled(attachment.isEnable());
        newValidator.setId(id);
        newValidator.setPublicKey(attachment.getPublicKey());
        newValidator.setFee(attachment.getFee());
        newValidator.setHeight(tx.getHeight());
        newValidator.setRewardId(attachment.getRewardId());
        repository.save(newValidator);
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

    //    @Override
    public List<ValidatorEntity> getAbsentNodes() {
        return repository.getAllWith(config.getCurrentConfig().getAbsentPeriod(), true);
    }

    @Override
    public void absentValidator(AccountId validatorId) {

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
        long punishmentAmount = stakeholderService.punishByzantineStakeholders(validatorId, height);
        ledgerService.add(new LedgerRecord(height, -punishmentAmount, LedgerRecord.Type.BYZANTINE_FINE, null, validatorId));
        byId.setVotePower(0);
        byId.setVotes(0);
        repository.save(byId);
        return punishmentAmount;
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
        if (byId.getAbsentFor() >= config.getCurrentConfig().getAbsentPeriod()) {
            byId.setEnabled(false);
            punishmentAmount = stakeholderService.punishStakeholders(validatorId, height);
            ledgerService.add(new LedgerRecord(height, -punishmentAmount, LedgerRecord.Type.ABSENT_FINE, null, validatorId));
            byId.setVotePower(byId.getVotePower() - punishmentAmount);
        } else {
            byId.setAbsentFor(byId.getAbsentFor() + 1);
            ledgerService.add(new LedgerRecord(height, byId.getAbsentFor(), LedgerRecord.Type.ABSENT, null, validatorId));
        }
        repository.save(byId);
        return punishmentAmount;
    }

    @Override
    public void distributeReward(List<ValidatorEntity> fairValidators, long rewardAmount, long height) {
        long totalVotePower = fairValidators.stream().mapToLong(ValidatorEntity::getVotePower).sum();
        for (ValidatorEntity fairValidator : fairValidators) {
            BigDecimal validatorPercent = BigDecimal.valueOf(fairValidator.getVotePower()).divide(BigDecimal.valueOf(totalVotePower), RoundingMode.DOWN);
            BigDecimal validatorReward = validatorPercent.multiply(BigDecimal.valueOf(rewardAmount));
            BigDecimal validatorFee = validatorReward.multiply(BigDecimal.valueOf(fairValidator.getFee())).divide(BigDecimal.valueOf(10000), RoundingMode.DOWN);
            fairValidator.setHeight(height);
            fairValidator.setVotePower(fairValidator.getVotePower() + validatorReward.toBigInteger().longValueExact());
            fairValidator.setAbsentFor(0);
            accountService.addToBalance(fairValidator.getId(), validatorFee.longValueExact(), height);
            BigDecimal stakeholdersReward = validatorReward.subtract(validatorFee);
            stakeholderService.distributeRewardForValidator(fairValidator.getId(), stakeholdersReward);
            repository.save(fairValidator);
        }
    }

    @Override
    public ValidatorEntity get(AccountId fairValidator) {
        return null;
    }

}
