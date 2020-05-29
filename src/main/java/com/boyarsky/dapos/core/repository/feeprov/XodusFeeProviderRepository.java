package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XodusFeeProviderRepository extends XodusAbstractRepository<FeeProvider> implements FeeProviderRepository {
    private static final String storeName = "fee-provider";

    @Autowired
    public XodusFeeProviderRepository(XodusRepoContext context) {
        super(storeName, false, context);
    }

    @Override
    protected void storeToDbEntity(Entity e, FeeProvider feeProvider) {
        e.setProperty("balance", feeProvider.getBalance());
        e.setProperty("state", feeProvider.getState().getCode());
        e.setProperty("account", Convert.toHexString(feeProvider.getAccount().getAddressBytes()));
        e.setProperty("fromConfig", Convert.toHexString(Convert.toBytes(feeProvider.getFromFeeConfig())));
        e.setProperty("toConfig", Convert.toHexString(Convert.toBytes(feeProvider.getToFeeConfig())));
        e.setProperty("id", feeProvider.getId());
    }

    @Override
    protected FeeProvider doMap(Entity entity) {
        FeeProvider feeProvider = new FeeProvider();
        feeProvider.setId((Long) entity.getProperty("id"));
        feeProvider.setAccount(AccountId.fromBytes(Convert.parseHexString((String) entity.getProperty("account"))));
        feeProvider.setBalance((Long) entity.getProperty("balance"));
        feeProvider.setFromFeeConfig(new PartyFeeConfig(Convert.toBuff(Convert.parseHexString((String) entity.getProperty("fromConfig")))));
        feeProvider.setToFeeConfig(new PartyFeeConfig(Convert.toBuff(Convert.parseHexString((String) entity.getProperty("toConfig")))));
        feeProvider.setState(State.ofCode((Byte) entity.getProperty("state")));
        return feeProvider;
    }

    @Override
    @Transactional(readonly = true)
    public FeeProvider get(long id) {
        return map(getByDbParams(List.of(new DbParamImpl("id", id))));
    }

    @Override
    @Transactional(readonly = true)
    public List<FeeProvider> getAll() {
        return super.getAll();
    }

    @Override
    public List<FeeProvider> getAll(State state) {
        return super.getAll(new DbParamImpl("state", state.getCode()));
    }


    @Override
    @Transactional(readonly = true)
    public List<FeeProvider> getByAccount(AccountId id) {
        return getAll(new DbParamImpl("account", Convert.toHexString(id.getAddressBytes())));
    }
}
