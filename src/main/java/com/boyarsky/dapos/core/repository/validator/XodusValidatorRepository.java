package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.DbParam;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XodusValidatorRepository extends XodusAbstractRepository<ValidatorEntity> implements ValidatorRepository {

    @Autowired
    public XodusValidatorRepository(XodusRepoContext context) {
        super("validator", true, context);
    }

    @Override
    protected void storeToDbEntity(Entity e, ValidatorEntity validatorEntity) {
        e.setProperty("fee", validatorEntity.getFee());
        e.setProperty("id", Convert.toHexString(validatorEntity.getId().getAddressBytes()));
        e.setProperty("publicKey", Convert.toHexString(validatorEntity.getPublicKey()));
        e.setProperty("power", validatorEntity.getVotePower());
        e.setProperty("votes", validatorEntity.getVotes());
        e.setProperty("absent", validatorEntity.getAbsentFor());
        e.setProperty("rewardAddress", Convert.toHexString(validatorEntity.getRewardId().getAddressBytes()));
        e.setProperty("enabled", validatorEntity.isEnabled());
    }

    @Override
    protected ValidatorEntity doMap(Entity e) {
        ValidatorEntity validatorEntity = new ValidatorEntity();
        validatorEntity.setVotePower((Long) e.getProperty("power"));
        validatorEntity.setId(AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("id"))));
        validatorEntity.setFee((Integer) e.getProperty("fee"));
        validatorEntity.setEnabled((Boolean) e.getProperty("enabled"));
        validatorEntity.setVotes((Integer) e.getProperty("votes"));
        validatorEntity.setPublicKey(Convert.parseHexString((String) e.getProperty("publicKey")));
        validatorEntity.setRewardId(AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("rewardAddress"))));
        validatorEntity.setAbsentFor((Long) e.getProperty("absent"));
        return validatorEntity;
    }

    @Override
    protected List<DbParam> idParams(ValidatorEntity value) {
        return List.of(new DbParamImpl("id", Convert.toHexString(value.getId().getAddressBytes())));
    }

    @Override
    @Transactional(readonly = true)
    public List<ValidatorEntity> getAll() {
        return CollectionUtils.toList(getTx().sort("validator", "power", getTx().getAll("validator"), false), this::map);
    }

    @Override
    @Transactional(readonly = true)
    public List<ValidatorEntity> getAll(long height) {
        return CollectionUtils.toList(getTx().sort("validator", "power", getTx().find("validator", "height", height, Long.MAX_VALUE), false), this::map);
    }

    @Override
    @Transactional(readonly = true)
    public ValidatorEntity getById(AccountId id) {
        return get(new DbParamImpl("id", Convert.toHexString(id.getAddressBytes())));
    }
}
