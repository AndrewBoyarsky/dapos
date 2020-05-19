package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.DbParam;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
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
        e.setProperty("delegatedBalance", validatorEntity.getVotePower());
        e.setProperty("enabled", validatorEntity.isEnabled());
    }

    @Override
    protected ValidatorEntity doMap(Entity e) {
        ValidatorEntity validatorEntity = new ValidatorEntity();
        validatorEntity.setVotePower((Long) e.getProperty("delegatedBalance"));
        validatorEntity.setId(AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("id"))));
        validatorEntity.setFee((Long) e.getProperty("fee"));
        validatorEntity.setEnabled((Boolean) e.getProperty("enabled"));
        return validatorEntity;
    }

    @Override
    protected List<DbParam> idParams(ValidatorEntity value) {
        return List.of(new DbParamImpl("id", Convert.toHexString(value.getId().getAddressBytes())));
    }

    @Override
    public List<ValidatorEntity> getAll() {
        return super.getAll();
    }

    @Override
    public List<ValidatorEntity> getAllWith(long absentFor, boolean enabled) {
        return getAll(new DbParamImpl("absentFor", absentFor), new DbParamImpl("enabled", enabled));
    }

    @Override
    public ValidatorEntity getById(AccountId id) {
        return get(new DbParamImpl("id", Convert.toHexString(id.getAddressBytes())));
    }
}
