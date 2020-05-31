package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.DbParam;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.Sort;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XodusVoteRepository extends XodusAbstractRepository<VoteEntity> implements VoteRepository {

    public static final String ENTITY_NAME = "vote";
    private final Sort defaultSort = new Sort();

    @Autowired
    protected XodusVoteRepository(@NonNull XodusRepoContext context) {
        super(ENTITY_NAME, true, context);
        defaultSort.add("totalStake", false);
    }

    @Override
    protected void storeToDbEntity(Entity e, VoteEntity voteEntity) {
        e.setProperty("id", Convert.toHexString(voteEntity.getAccountId().getAddressBytes()));
        e.setProperty("validatorId", Convert.toHexString(voteEntity.getValidatorId().getAddressBytes()));
        e.setProperty("totalStake", voteEntity.getTotalPower());
    }

    @Override
    protected VoteEntity doMap(Entity e) {
        VoteEntity voteEntity = new VoteEntity();
        voteEntity.setAccountId(AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("id"))));
        voteEntity.setValidatorId(AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("validatorId"))));
        voteEntity.setTotalPower((Long) e.getProperty("totalStake"));
        return voteEntity;
    }

    @Override
    @Transactional(readonly = true)
    public VoteEntity getBy(AccountId validatorId, AccountId voterId) {
        return CollectionUtils.requireAtMostOne(getAll(new DbParamImpl("id", Convert.toHexString(voterId.getAddressBytes())), new DbParamImpl("validatorId", Convert.toHexString(validatorId.getAddressBytes()))));
    }

    @Override
    @Transactional(readonly = true)
    public List<VoteEntity> getAllVotesForValidator(AccountId validatorId) {
        return getAllVotesForValidator(validatorId, null);
    }

    @Override
    @Transactional(readonly = true)
    public List<VoteEntity> getAllVotesForValidator(AccountId validatorId, Pagination pagination) {
        return getAll(defaultSort, pagination, new DbParamImpl("validatorId", Convert.toHexString(validatorId.getAddressBytes())));
    }

    @Override
    @Transactional(readonly = true)
    public long countAllVotesForValidator(AccountId validatorId) {
        return getAllEntities(new DbParamImpl("validatorId", Convert.toHexString(validatorId.getAddressBytes()))).size();
    }

    @Override
    @Transactional(readonly = true)
    public VoteEntity minVoteForValidator(AccountId validatorId) {
        return map(CollectionUtils.requireAtMostOne(getTx()
                .sort(ENTITY_NAME, "totalStake",
                        getAllEntities(new DbParamImpl("validatorId", Convert.toHexString(validatorId.getAddressBytes()))), true)
                .take(1)));
    }

    @Override
    protected List<DbParam> idParams(VoteEntity value) {
        return List.of(
                new DbParamImpl("id", Convert.toHexString(value.getAccountId().getAddressBytes())),
                new DbParamImpl("validatorId", Convert.toHexString(value.getValidatorId().getAddressBytes())));
    }

    @Override
    @Transactional(readonly = true)
    public List<VoteEntity> getAllVotesForVoter(AccountId voterId) {
        return getAllVotesForVoter(voterId, null);
    }

    @Override
    @Transactional(readonly = true)
    public List<VoteEntity> getAllVotesForVoter(AccountId voterId, Pagination pagination) {
        return getAll(pagination, new DbParamImpl("id", Convert.toHexString(voterId.getAddressBytes())));
    }

    @Override
    @Transactional(readonly = true)
    public List<VoteEntity> getAll(Pagination pagination) {
        return super.getAll(pagination);
    }
}
