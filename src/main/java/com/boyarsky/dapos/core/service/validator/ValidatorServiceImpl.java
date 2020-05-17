package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.validator.ValidatorRepository;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ValidatorAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private ValidatorRepository repository;
    private BlockchainConfig config;

    @Autowired
    public ValidatorServiceImpl(ValidatorRepository repository, BlockchainConfig config) {
        this.repository = repository;
        this.config = config;
    }

    @Override
    public void controlValidator(Transaction tx, ValidatorAttachment attachment) {
        AccountId id;
        if (attachment.getId() == null) {
            id = new AccountId(CryptoUtils.validatorAddress(attachment.getPublicKey()));
        } else {
            id = attachment.getId();
        }
        ValidatorEntity existing = repository.getById(id);
        if (existing == null) {
            existing = new ValidatorEntity();
            existing.setId(id);
        }
        existing.setHeight(tx.getHeight());
        existing.setEnabled(attachment.isEnable());
        existing.setFee(attachment.getFee());
        repository.save(existing);
    }

    @Override
    public List<ValidatorEntity> updatedAt(long height) {
        return repository.getAllAt(height);
    }

    @Override
    public List<ValidatorEntity> getAbsentNodes() {
        return repository.getAllWith(config.getCurrentConfig().getAbsentPeriod(), true);
    }

}
